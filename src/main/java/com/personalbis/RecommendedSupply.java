package com.personalbis;
public final class RecommendedSupply {
 public final int itemId,quantity; public final String name,reason;
 public RecommendedSupply(int id,int q,String n){this(id,q,n,"Owned supply");}
 public RecommendedSupply(int id,int q,String n,String r){itemId=id;quantity=q;name=n;reason=r==null?"":r;}
}
