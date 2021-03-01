package org.point85.workschedule;

/**
 *  youngil 추가
 *  TimePeriod 타입을 구분하기 위해 사용
 */
public enum EnumTimePeriod {

    SHIFT("SHIFT"),          //SHIFT
    DAYOFF("DAYOFF"),  //DAYOFF
    BREAK("BREAK");      //BREAK

    final private String name;

    public String getName() {
        return name;
    }

    EnumTimePeriod(String name){
        this.name = name;
    }

}
