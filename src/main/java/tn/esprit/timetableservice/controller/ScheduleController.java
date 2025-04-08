package tn.esprit.timetableservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.timetableservice.entities.Schedule;
import tn.esprit.timetableservice.services.ScheduleService;

import java.util.List;


@RestController
//@RequestMapping("/api/timetable-service")
@RequestMapping("/timetable-service")
//@CrossOrigin(origins = "http://localhost:4200")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping("/generate")
    public ResponseEntity<List<List<Schedule>>> generateSchedule(@RequestParam(defaultValue = "10") int numClasses) {
        List<List<Schedule>> timetables = scheduleService.runGeneticAlgorithm(100, numClasses);
        return ResponseEntity.ok(timetables);
    }
}

