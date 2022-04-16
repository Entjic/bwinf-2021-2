package com.franosch.bwinf.muellabfuhr;

import com.franosch.bwinf.muellabfuhr.io.FileReader;
import com.franosch.bwinf.muellabfuhr.io.ResultFileWriter;
import com.franosch.bwinf.muellabfuhr.model.Result;
import com.franosch.bwinf.muellabfuhr.model.graph.Cycle;

import java.util.Collections;
import java.util.List;

public class WeeklyScheduleGenerator {
    private final static String TEST_RESOURCES = "muellabfuhr/src/test/resources/";


    public void findWeeklySchedule(int testCase) {
        FileReader reader = new FileReader(testCase, TEST_RESOURCES);
        Solver solver = new Solver();
        solver.initGraph(reader);
        System.out.println(solver.findOddDegree().size());
        solver.makeEven();
        List<Cycle> cpp = solver.solveChinesePostmanProblem();
        List<Result> results = solver.allocate(5, cpp);
        Collections.sort(results);
        Result result = results.get(0);
        System.out.println("---------");
        System.out.println("BEST RESULT");
        System.out.println("---------");
        System.out.println();
        System.out.println(result);
        System.out.println();
        System.out.println("---------");
        System.out.println("BEST RESULT");
        System.out.println("---------");
        ResultFileWriter resultFileWriter = new ResultFileWriter(result);
        resultFileWriter.writeFile();
    }


}