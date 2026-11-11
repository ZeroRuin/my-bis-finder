package com.personalbis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Runs every standalone self-test from {@code gradle test}, so one command (and CI)
 * covers them all. Each self-test still keeps its own Gradle task for running it alone.
 *
 * <p>Each self-test runs in its own JVM, exactly like its Gradle task. Several of them
 * rely on static data being loaded fresh (for example SlayerGsonSelfTest), so sharing
 * one JVM would make results depend on test order.
 */
class SelfTestSuite
{
    private static final class SelfTest
    {
        final Class<?> type;
        final List<String> jvmArgs;

        SelfTest(Class<?> type, String... jvmArgs)
        {
            this.type = type;
            this.jvmArgs = Arrays.asList(jvmArgs);
        }
    }

    /** Programs with a main() that are deliberately not run as tests. */
    private static final Set<String> NOT_SELF_TESTS = Set.of(
        "SelfTestSuite",           // this file
        "PersonalBisPluginTest",   // launches the RuneLite dev client
        "SearchSnapshot",          // prints search results for before/after comparison
        "SlayerLocationCoverage",  // writes a coverage report
        "SlayerUnresolvedAudit");  // writes an audit file

    private static final long TIMEOUT_MINUTES = 5;

    static List<SelfTest> selfTests()
    {
        return Arrays.asList(
            new SelfTest(BankWithdrawalSelfTest.class),
            new SelfTest(BlowpipeAmmoPlanSelfTest.class),
            new SelfTest(CombatSearchSelfTest.class),
            new SelfTest(CombatStyleLabelsSelfTest.class),
            new SelfTest(DefenceFillSelfTest.class),
            new SelfTest(DefenceReductionBankItemsSelfTest.class),
            new SelfTest(DefenceReductionPlanSelfTest.class),
            new SelfTest(FracturedArchiveSelfTest.class),
            new SelfTest(GroupStorageSnapshotSelfTest.class),
            new SelfTest(HpStateTtkSelfTest.class),
            new SelfTest(HueycoatlSelfTest.class),
            new SelfTest(ItemExclusionSelfTest.class),
            new SelfTest(LoadoutStatsSizingSelfTest.class),
            new SelfTest(ManualDefenceReductionSelfTest.class),
            new SelfTest(MechanicsSelfTest.class),
            new SelfTest(PoweredStanceSelfTest.class),
            new SelfTest(RaidOptionsSelfTest.class),
            new SelfTest(ReviewedSlayerLocationsSelfTest.class),
            new SelfTest(SalveRoundingSelfTest.class),
            new SelfTest(SearchBehaviourSelfTest.class),
            new SelfTest(SearchBruteForceSelfTest.class),
            new SelfTest(SlayerGsonSelfTest.class),
            new SelfTest(SlayerTaskSelfTest.class),
            new SelfTest(SpecialAttackSelfTest.class),
            new SelfTest(TargetStyleImmunitySelfTest.class),
            new SelfTest(TheatreOfBloodSelfTest.class),
            // Deliberately exercised with a small heap, as in its Gradle task.
            new SelfTest(TtkPerformanceSelfTest.class, "-Xmx64m"),
            new SelfTest(TwistedBowRoundingSelfTest.class),
            new SelfTest(VardorvisHpSelfTest.class),
            new SelfTest(VardorvisSelfTest.class),
            new SelfTest(VardorvisShortlistSelfTest.class),
            new SelfTest(VoidEquipmentSelfTest.class),
            new SelfTest(VoidLoadoutFamiliesSelfTest.class));
    }

    @TestFactory
    Stream<DynamicTest> selfTestsPass()
    {
        return selfTests().stream().map(test ->
            DynamicTest.dynamicTest(test.type.getSimpleName(), () -> runInFreshJvm(test)));
    }

    private static void runInFreshJvm(SelfTest test) throws Exception
    {
        String classpath = System.getProperty("selftest.classpath");
        assertTrue(classpath != null && !classpath.isEmpty(),
            "selftest.classpath is not set; run through `gradle test`");

        List<String> command = new ArrayList<>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        command.add("-ea");
        command.add("-Djava.awt.headless=true");
        command.addAll(test.jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(test.type.getName());

        File log = File.createTempFile(test.type.getSimpleName(), ".log");
        log.deleteOnExit();
        Process process = new ProcessBuilder(command)
            .directory(new File(System.getProperty("user.dir")))
            .redirectErrorStream(true)
            .redirectOutput(log)
            .start();

        if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES))
        {
            process.destroyForcibly();
            throw new AssertionError(test.type.getSimpleName() + " timed out after "
                + TIMEOUT_MINUTES + " minutes\n" + tail(log));
        }
        if (process.exitValue() != 0)
        {
            throw new AssertionError(test.type.getSimpleName() + " failed (exit "
                + process.exitValue() + ")\n" + tail(log));
        }
    }

    /** The last lines of a self-test's output, enough to show the failing check. */
    private static String tail(File log) throws IOException
    {
        List<String> lines = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
        return String.join("\n", lines.subList(Math.max(0, lines.size() - 40), lines.size()));
    }

    /** Fails when a new self-test is added but not listed above, so it can't be silently skipped. */
    @Test
    void everySelfTestIsListed() throws IOException
    {
        Path dir = Paths.get("src/test/java/com/personalbis");
        Set<String> found;
        try (Stream<Path> files = Files.list(dir))
        {
            found = files
                .filter(file -> file.toString().endsWith(".java"))
                .filter(file -> read(file).contains("static void main("))
                .map(file -> file.getFileName().toString().replace(".java", ""))
                .filter(name -> !NOT_SELF_TESTS.contains(name))
                .collect(Collectors.toCollection(TreeSet::new));
        }
        Set<String> listed = selfTests().stream()
            .map(test -> test.type.getSimpleName())
            .collect(Collectors.toCollection(TreeSet::new));
        assertEquals(found, listed, "Add new self-tests to SelfTestSuite.selfTests() (or NOT_SELF_TESTS)");
    }

    private static String read(Path file)
    {
        try
        {
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
