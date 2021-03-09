package org.point85.workschedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

    // 해당날짜 에 여러개의 팀이 근무 투입될때
    // 한팀은 주간근무, 한팀은 주간근무, 한팀은 주간근무, 한팀은 야간근무 일때
    // 지정된 로테이션 (주,주,주,야,비,야,,비) 중 어떤 근무의 시작에 해당하는지 설정하고
    // preriods 를 변경하여 설정한다..
    public  List<TimePeriod> calculatePeriods(Rotation rotation,int startIndex){
        List<TimePeriod> periods = rotation.getPeriods();

        int periodsSize = periods.size();
        int loopLen = periods.size()-1;

        List<TimePeriod>periodsTmp= new ArrayList<>();
        int loopCnt=1; //루프를 몇번돌았는지확인
        for(int i=startIndex;i<=loopLen;i++){
            periodsTmp.add(periods.get(i));
            loopCnt++;
        }

        //나머지
        if(loopCnt<periodsSize){
            for(int j=0;j<periodsSize-loopCnt;j++){
                periodsTmp.add(periods.get(j));
            }
        }
         return periodsTmp;
    }

    public void firstTest() throws Exception {
        System.out.println("=============== 테스트");

        String description ="first test";
        WorkSchedule schedule = new WorkSchedule("DNO Plan", description);

        // 주간 근무, 07:00에 시작  12 시간 동안근무
        Shift day = schedule.createShift("Day", "Day shift", LocalTime.of(7, 0, 0), Duration.ofHours(12));

        // 야간 근무, 19:00에 시작 12 시간 동안근무
        Shift night = schedule.createShift("Night", "Night shift", LocalTime.of(19, 0, 0), Duration.ofHours(12));


        // rotation (주주주야비야비 예제)
        Rotation rotation = new Rotation("DNO", "DNO");
        rotation.addSegment(day, 3, 0,0);  //주간근무 3일
        rotation.addSegment(night, 1, 1,0); //야간근무 1일 다음날비번
        rotation.addSegment(night, 1, 1,0); //야간근무 1일 다음날비번


        calculatePeriods(rotation,1);

        // rotation (주주야비야비주 예제)
        Rotation rotation2 = new Rotation("DNO", "DNO");
        rotation2.addSegment(day, 2, 0,0);  //주간근무 2일
        rotation2.addSegment(night, 1, 1,0); //야간근무  다음날 비번
        rotation2.addSegment(night, 1, 1,0); //야간근무  다음날 비번
        rotation2.addSegment(day, 1, 0,0);  //주간근무 1일


        // 당직주기 시작일자
        LocalDate rotationStartDate = LocalDate.of(2021,2,28);



       schedule.createTeam("Team 1", "First team", rotation, rotationStartDate);
       schedule.createTeam("Team 2", "Second team", rotation2, rotationStartDate);
       // schedule.createTeam("Team 3", "Third team", rotation, rotationStartDate.plusDays(2));

        //System.out.println("");

        schedule.printShiftInstances(LocalDate.of(2021, 3, 1), LocalDate.of(2021, 3, 31));


        // 해당날자에 근무팀별 근무상황을 가져온다.
        //List<ShiftInstance> instances = schedule.getShiftInstancesForDay(LocalDate.of(2021, 2, 16));





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
