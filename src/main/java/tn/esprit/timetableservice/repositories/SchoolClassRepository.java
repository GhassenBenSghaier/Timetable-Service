package tn.esprit.timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.timetableservice.entities.SchoolClass;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long>
{
}
