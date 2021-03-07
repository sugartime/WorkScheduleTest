package org.point85.workschedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class Main {

    /**
     *   동작방법
     *
     *
     *
     */

    public static void main(String[] args) throws Exception {
	// write your code here
        Main m = new Main();
        m.firstTest();
    }

    public void firstTest() throws Exception {
        System.out.println("=============== 테스트");

        String description ="first test";
        WorkSchedule schedule = new WorkSchedule("DNO Plan", description);

        // 주간 근무, 07:00에 시작  12 시간 동안근무
        Shift day = schedule.createShift("Day", "Day shift", LocalTime.of(7, 0, 0), Duration.ofHours(12));

        // 야간 근무, 19:00에 시작 12 시간 동안근무
        Shift night = schedule.createShift("Night", "Night shift", LocalTime.of(19, 0, 0), Duration.ofHours(12));


        // rotation (주야비휴 예제)
        Rotation rotation = new Rotation("DNO", "DNO");
        rotation.addSegment(day, 3, 0);  //주간근무 다음날 휴무 없음
        rotation.addSegment(night, 1, 1); //하루야간근무 2틀휴무
        rotation.addSegment(night, 1, 1); //하루야간근무 2틀휴무

        // 당직주기 시작일자
        LocalDate rotationStartDate = LocalDate.of(2021,2,1);



       schedule.createTeam("Team 1", "First team", rotation, rotationStartDate);
       //schedule.createTeam("Team 2", "Second team", rotation, rotationStartDate.plusDays(1));
       // schedule.createTeam("Team 3", "Third team", rotation, rotationStartDate.plusDays(2));

        //System.out.println("");

        schedule.printShiftInstances(LocalDate.of(2021, 2, 1), LocalDate.of(2021, 3, 31));


        // 해당날자에 근무팀별 근무상황을 가져온다.
        List<ShiftInstance> instances = schedule.getShiftInstancesForDay(LocalDate.of(2021, 2, 16));





        // 아래는 07:00부터 3 일 동안의 근무 시간을 얻기 위해 다음 메소드 호출 테스트
//        LocalDate referenceDate= LocalDate.of(2021,2,1);
//        LocalDateTime from = LocalDateTime.of(referenceDate, LocalTime.of(7, 0, 0));
//        LocalDateTime to = from.plusDays(3);
//
//        System.out.println("=============== from :"+from+" to:"+to);
//
//        Duration duration = schedule.calculateWorkingTime(from,to);
//
//        System.out.println("=============== duration :"+duration.getSeconds());


        //System.out.println("");
    }
}
