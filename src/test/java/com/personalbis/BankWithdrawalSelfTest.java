package com.personalbis;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

/** Local API fixtures only; never sends game actions. */
public final class BankWithdrawalSelfTest {
    public static void main(String[] args) throws Exception {
        for (MenuAction action : new MenuAction[]{MenuAction.CC_OP, MenuAction.CC_OP_LOW_PRIORITY})
        for (int container : new int[]{InterfaceID.Bankmain.ITEMS, InterfaceID.SharedBank.ITEMS}) {
            check(action,container,385,385,385,0,false,false,false,"valid withdrawal");
            check(action,container,385,373,373,0,false,false,true,"widget reused");
            check(action,container,385,385,373,0,false,false,true,"bank slot changed");
            check(action,container,385,385,385,1,false,false,true,"out of bounds");
            check(action,container,385,385,385,-1,false,false,true,"missing slot");
            check(action,container,-1,385,385,0,false,false,true,"unknown item");
            check(action,container,385,385,385,0,true,false,true,"hidden widget");
            check(action,container,385,385,385,0,false,true,true,"unknown bank");
        }
        System.out.println("ALL BANK WITHDRAWAL SELF-TESTS PASSED");
    }
    private static void check(MenuAction action, int container, int clicked, int displayed, int stored, int slot,
        boolean hidden, boolean missing, boolean blocked, String label) throws Exception {
        Widget widget=proxy(Widget.class,(m,a)-> {
            switch(m) {
                case "getId": return container;
                case "getParentId": return -1;
                case "getItemId": return displayed;
                case "isHidden": return hidden;
                default: throw new AssertionError(m);
            }
        });
        ItemContainer bank=proxy(ItemContainer.class,(m,a)-> {
            if(m.equals("getItems")) return new Item[]{new Item(stored,1)};
            throw new AssertionError(m);
        });
        Client client=proxy(Client.class,(m,a)-> {
            if(m.equals("getItemContainer")) return missing?null:bank;
            throw new AssertionError(m);
        });
        MenuEntry menu=proxy(MenuEntry.class,(m,a)-> {
            switch(m) {
                case "getWidget": return widget;
                case "getOption": return "Withdraw-1";
                case "getType": return action;
                case "getParam0": return slot;
                case "getParam1": return container;
                case "getItemId": return clicked;
                default: throw new AssertionError("unexpected menu access/mutation: "+m);
            }
        });
        PersonalBisBankFilter filter=new PersonalBisBankFilter(client,null,null,null,null,null);
        Field active=PersonalBisBankFilter.class.getDeclaredField("active");
        active.setAccessible(true); active.setBoolean(filter,true);
        MenuOptionClicked event=new MenuOptionClicked(menu);
        filter.onMenuOptionClicked(event);
        if(event.isConsumed()!=blocked) throw new AssertionError(label);
        active.setBoolean(filter,false);
        event=new MenuOptionClicked(menu); filter.onMenuOptionClicked(event);
        if(event.isConsumed()) throw new AssertionError("inactive planner: "+label);
        System.out.println("PASS "+label+" action="+action+" container="+container);
    }
    private interface Answer { Object invoke(String method,Object[] args); }
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type,Answer answer) {
        return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(o,m,a)-> {
            if(m.getName().equals("hashCode")) return System.identityHashCode(o);
            if(m.getName().equals("equals")) return o==a[0];
            return answer.invoke(m.getName(),a);
        });
    }
}
