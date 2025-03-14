package tn.esprit.timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.timetableservice.entities.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
