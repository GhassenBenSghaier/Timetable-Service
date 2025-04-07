//
//
//package tn.esprit.timetableservice.services;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import tn.esprit.timetableservice.entities.*;
//import tn.esprit.timetableservice.repositories.ClassroomRepository;
//import tn.esprit.timetableservice.repositories.SchoolClassRepository;
//import tn.esprit.timetableservice.repositories.SubjectRepository;
//import tn.esprit.timetableservice.repositories.TeacherRepository;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class ScheduleService {
//
//    private static final int POPULATION_SIZE = 4000;
//    private static final double MUTATION_RATE = 0.2;
//    private static final int SCHEDULES_PER_CLASS = 10; // 10 slots per class
//    private static final int MAX_GENERATIONS = 3000;
//    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
//
//    @Autowired private TeacherRepository teacherRepository;
//    @Autowired private ClassroomRepository classroomRepository;
//    @Autowired private SubjectRepository subjectRepository;
//    @Autowired private SchoolClassRepository schoolClassRepository;
//
//    private List<Schedule> createRandomScheduleForClass(SchoolClass schoolClass) {
//        List<Schedule> schedule = new ArrayList<>();
//        List<Teacher> teachers = teacherRepository.findAll();
//        List<Classroom> classrooms = classroomRepository.findAll();
//        List<Subject> subjects = subjectRepository.findAll();
//        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
//        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");
//
//        if (teachers.isEmpty() || classrooms.isEmpty() || subjects.isEmpty()) {
//            throw new IllegalStateException("Cannot create schedule: Teachers, Classrooms, or Subjects list is empty.");
//        }
//
//        Random random = new Random();
//        List<Subject> subjectsToSchedule = getSubjectsForSpecialty(schoolClass.getSpecialty(), subjects);
//        Collections.shuffle(subjectsToSchedule);
//
//        Map<String, Teacher> subjectTeacherMap = new HashMap<>();
//        Map<String, Set<String>> scheduledTimes = new HashMap<>();
//
//        for (Subject subject : subjectsToSchedule) {
//            String subjectName = subject.getName();
//            boolean isDoubleSubject = isDoubleSubject(schoolClass.getSpecialty(), subjectName);
//
//            Teacher teacher = subjectTeacherMap.get(subjectName);
//            if (teacher == null) {
//                List<Teacher> eligibleTeachers = teachers.stream()
//                        .filter(t -> t.getSubjects().contains(subject))
//                        .collect(Collectors.toList());
//                if (eligibleTeachers.isEmpty()) {
//                    logger.warn("No teacher available for subject: {}", subjectName);
//                    continue;
//                }
//                teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
//                subjectTeacherMap.put(subjectName, teacher);
//            }
//
//            for (int attempt = 0; attempt < 100; attempt++) {
//                String day = days.get(random.nextInt(days.size()));
//                String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
//                String timeKey = day + "-" + timeSlot;
//
//                if (scheduledTimes.containsKey(timeKey)) continue;
//                if (isDoubleSubject && schedule.stream()
//                        .filter(s -> s.getSubject().equals(subject))
//                        .anyMatch(s -> s.getDay().equals(day))) continue;
//
//                Classroom classroom = findAvailableClassroom(timeKey, classrooms, schedule);
//                if (classroom == null) {
//                    logger.debug("No classroom available for {} at {}", subjectName, timeKey);
//                    continue;
//                }
//
//                Schedule newSchedule = new Schedule(teacher, classroom, subject, schoolClass, day, timeSlot);
//                schedule.add(newSchedule);
//                scheduledTimes.put(timeKey, new HashSet<>());
//                break;
//            }
//        }
//        if (schedule.size() < SCHEDULES_PER_CLASS) {
//            logger.warn("Generated only {} slots for class {}, expected {}", schedule.size(), schoolClass.getName(), SCHEDULES_PER_CLASS);
//        }
//        return schedule;
//    }
//
//    private List<Subject> getSubjectsForSpecialty(String specialty, List<Subject> allSubjects) {
//        List<Subject> subjectsToSchedule = new ArrayList<>();
//        Subject math = allSubjects.stream().filter(s -> s.getName().equals("Math")).findFirst().orElse(null);
//        Subject physics = allSubjects.stream().filter(s -> s.getName().equals("Physics")).findFirst().orElse(null);
//        Subject english = allSubjects.stream().filter(s -> s.getName().equals("English")).findFirst().orElse(null);
//        Subject history = allSubjects.stream().filter(s -> s.getName().equals("History")).findFirst().orElse(null);
//        Subject chemistry = allSubjects.stream().filter(s -> s.getName().equals("Chemistry")).findFirst().orElse(null);
//        Subject french = allSubjects.stream().filter(s -> s.getName().equals("French")).findFirst().orElse(null);
//        Subject geography = allSubjects.stream().filter(s -> s.getName().equals("Geography")).findFirst().orElse(null);
//        Subject biology = allSubjects.stream().filter(s -> s.getName().equals("Biology")).findFirst().orElse(null);
//        Subject philosophy = allSubjects.stream().filter(s -> s.getName().equals("Philosophy")).findFirst().orElse(null);
//        Subject technology = allSubjects.stream().filter(s -> s.getName().equals("Technology")).findFirst().orElse(null);
//
//        switch (specialty) {
//            case "Math":
//                if (math != null) { subjectsToSchedule.add(math); subjectsToSchedule.add(math); } // Math twice
//                if (physics != null) { subjectsToSchedule.add(physics); subjectsToSchedule.add(physics); } // Physics twice
//                if (english != null) subjectsToSchedule.add(english);
//                if (history != null) subjectsToSchedule.add(history);
//                if (chemistry != null) subjectsToSchedule.add(chemistry);
//                if (french != null) subjectsToSchedule.add(french);
//                if (geography != null) subjectsToSchedule.add(geography);
//                if (biology != null) subjectsToSchedule.add(biology);
//                break;
//            case "Lettre":
//                if (philosophy != null) { subjectsToSchedule.add(philosophy); subjectsToSchedule.add(philosophy); } // Philosophy twice
//                if (french != null) { subjectsToSchedule.add(french); subjectsToSchedule.add(french); } // French twice
//                if (english != null) subjectsToSchedule.add(english);
//                if (history != null) subjectsToSchedule.add(history);
//                if (chemistry != null) subjectsToSchedule.add(chemistry);
//                if (geography != null) subjectsToSchedule.add(geography);
//                if (math != null) subjectsToSchedule.add(math);
//                if (physics != null) subjectsToSchedule.add(physics);
//                break;
//            case "Technology":
//                if (technology != null) { subjectsToSchedule.add(technology); subjectsToSchedule.add(technology); } // Technology twice
//                if (math != null) { subjectsToSchedule.add(math); subjectsToSchedule.add(math); } // Math twice
//                if (english != null) subjectsToSchedule.add(english);
//                if (history != null) subjectsToSchedule.add(history);
//                if (chemistry != null) subjectsToSchedule.add(chemistry);
//                if (french != null) subjectsToSchedule.add(french);
//                if (geography != null) subjectsToSchedule.add(geography);
//                if (physics != null) subjectsToSchedule.add(physics);
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown specialty: " + specialty);
//        }
//        return subjectsToSchedule;
//    }
//
//    private boolean isDoubleSubject(String specialty, String subjectName) {
//        switch (specialty) {
//            case "Math":
//                return "Math".equals(subjectName) || "Physics".equals(subjectName);
//            case "Lettre":
//                return "Philosophy".equals(subjectName) || "French".equals(subjectName);
//            case "Technology":
//                return "Technology".equals(subjectName) || "Math".equals(subjectName);
//            default:
//                return false;
//        }
//    }
//
//    private Classroom findAvailableClassroom(String timeKey, List<Classroom> classrooms, List<Schedule> existing) {
//        Set<Long> usedClassrooms = existing.stream()
//                .filter(s -> (s.getDay() + "-" + s.getTimeSlot()).equals(timeKey))
//                .map(s -> s.getClassroom().getId())
//                .collect(Collectors.toSet());
//
//        return classrooms.stream()
//                .filter(c -> !usedClassrooms.contains(c.getId()))
//                .findFirst()
//                .orElse(null);
//    }
//
//    private List<List<Schedule>> initializePopulation(List<SchoolClass> classes) {
//        List<List<Schedule>> population = new ArrayList<>();
//        for (int i = 0; i < POPULATION_SIZE; i++) {
//            List<Schedule> individual = new ArrayList<>();
//            for (SchoolClass schoolClass : classes) {
//                individual.addAll(createRandomScheduleForClass(schoolClass));
//            }
//            population.add(individual);
//        }
//        return population;
//    }
//
//    public int calculateFitness(List<Schedule> individual) {
//        long startTime = System.nanoTime(); // Diagnostic: Start timing
//        int score = 0;
//        Map<String, Set<Long>> teacherSlots = new HashMap<>();
//        Map<String, Set<Long>> classroomSlots = new HashMap<>();
//        Map<Long, Map<String, Set<String>>> classTimeSlots = new HashMap<>();
//        Map<Long, Map<String, Integer>> classSubjectFrequency = new HashMap<>();
//        Map<Long, Map<String, Map<String, Integer>>> classSubjectDayFrequency = new HashMap<>();
//        Map<Long, Map<String, Set<Long>>> classSubjectTeachers = new HashMap<>();
//
//        for (Schedule s : individual) {
//            String timeKey = s.getDay() + "-" + s.getTimeSlot();
//            Long classId = s.getSchoolClass().getId();
//            String subjectName = s.getSubject().getName();
//            String specialty = s.getSchoolClass().getSpecialty();
//            Long teacherId = s.getTeacher().getId();
//
//            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//
//            if (!teacherSlots.get(timeKey).add(teacherId)) score -= 1000;
//            else score += 10;
//
//            if (!classroomSlots.get(timeKey).add(s.getClassroom().getId())) score -= 1000;
//            else score += 10;
//
//            classTimeSlots.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(timeKey, k -> new HashSet<>())
//                    .add(subjectName);
//
//            if (classTimeSlots.get(classId).get(timeKey).size() > 1) score -= 5000;
//
//            classSubjectFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                    .merge(subjectName, 1, Integer::sum);
//            classSubjectDayFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(subjectName, k -> new HashMap<>())
//                    .merge(s.getDay(), 1, Integer::sum);
//
//            // Optimized teacher consistency check
//            if (isDoubleSubject(specialty, subjectName)) {
//                classSubjectTeachers.computeIfAbsent(classId, k -> new HashMap<>())
//                        .computeIfAbsent(subjectName, k -> new HashSet<>())
//                        .add(teacherId);
//            }
//        }
//
//        // Post-loop checks for double subjects
//        for (Long classId : classSubjectFrequency.keySet()) {
//            String specialty = individual.stream()
//                    .filter(s -> s.getSchoolClass().getId().equals(classId))
//                    .findFirst().get().getSchoolClass().getSpecialty();
//            Map<String, Integer> freq = classSubjectFrequency.get(classId);
//            Map<String, Map<String, Integer>> dayFreq = classSubjectDayFrequency.get(classId);
//            Map<String, Set<Long>> teachers = classSubjectTeachers.getOrDefault(classId, new HashMap<>());
//
//            for (String subject : freq.keySet()) {
//                int frequency = freq.get(subject);
//                if (isDoubleSubject(specialty, subject)) {
//                    if (frequency > 2) score -= 5000;
//                    if (dayFreq.get(subject).values().stream().anyMatch(c -> c > 1)) score -= 5000;
//                    if (frequency >= 2 && teachers.get(subject).size() > 1) score -= 5000; // Only check when freq >= 2
//                    if (frequency == 2 && teachers.get(subject).size() == 1) score += 50;
//                } else if (frequency > 1) {
//                    score -= 5000;
//                }
//            }
//        }
//
//        long endTime = System.nanoTime(); // Diagnostic: End timing
//        logger.info("Fitness calculation took: {} ms", (endTime - startTime) / 1_000_000);
//        return score;
//    }
//
//    private List<Schedule> selectParent(List<List<Schedule>> population) {
//        Random random = new Random();
//        List<List<Schedule>> tournament = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            tournament.add(population.get(random.nextInt(population.size())));
//        }
//        return tournament.stream()
//                .max(Comparator.comparingInt(this::calculateFitness))
//                .orElse(null);
//    }
//
//    private List<Schedule> crossover(List<Schedule> parent1, List<Schedule> parent2) {
//        if (parent1 == null || parent2 == null) return new ArrayList<>();
//
//        Map<Long, List<Schedule>> parent1Classes = parent1.stream()
//                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));
//        Map<Long, List<Schedule>> parent2Classes = parent2.stream()
//                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));
//
//        List<SchoolClass> classes = schoolClassRepository.findAll();
//        List<Schedule> child = new ArrayList<>();
//        Random random = new Random();
//
//        for (Long classId : parent1Classes.keySet()) {
//            if (random.nextBoolean()) {
//                child.addAll(parent1Classes.get(classId));
//            } else {
//                child.addAll(parent2Classes.getOrDefault(classId, Collections.emptyList()));
//            }
//        }
//        return repairConflicts(child, classes);
//    }
//
//    private void mutate(List<Schedule> schedule) {
//        if (schedule.isEmpty()) return;
//
//        Random random = new Random();
//        Schedule toMutate = schedule.get(random.nextInt(schedule.size()));
//        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
//        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");
//
//        for (int attempt = 0; attempt < 50; attempt++) {
//            String newDay = days.get(random.nextInt(days.size()));
//            String newTime = timeSlots.get(random.nextInt(timeSlots.size()));
//
//            boolean conflict = schedule.stream()
//                    .anyMatch(s -> s != toMutate &&
//                            s.getDay().equals(newDay) &&
//                            s.getTimeSlot().equals(newTime) &&
//                            (s.getTeacher().equals(toMutate.getTeacher()) ||
//                                    s.getClassroom().equals(toMutate.getClassroom())));
//
//            if (!conflict) {
//                toMutate.setDay(newDay);
//                toMutate.setTimeSlot(newTime);
//                break;
//            }
//        }
//    }
//
//    private List<Schedule> repairConflicts(List<Schedule> individual, List<SchoolClass> classes) {
//        List<Schedule> repaired = new ArrayList<>();
//        Map<String, Set<Long>> teacherSlots = new HashMap<>();
//        Map<String, Set<Long>> classroomSlots = new HashMap<>();
//        Map<Long, Map<String, Integer>> subjectCounts = new HashMap<>();
//        List<Teacher> allTeachers = teacherRepository.findAll();
//        List<Classroom> allClassrooms = classroomRepository.findAll();
//
//        for (Schedule s : individual) {
//            String timeKey = s.getDay() + "-" + s.getTimeSlot();
//            Long classId = s.getSchoolClass().getId();
//            String subjectName = s.getSubject().getName();
//
//            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//
//            if (teacherSlots.get(timeKey).contains(s.getTeacher().getId())) {
//                Teacher newTeacher = findAvailableTeacher(s.getSubject(), timeKey, teacherSlots);
//                if (newTeacher != null) s.setTeacher(newTeacher);
//            }
//
//            if (classroomSlots.get(timeKey).contains(s.getClassroom().getId())) {
//                Classroom newClassroom = findAvailableClassroom(timeKey, allClassrooms, repaired);
//                if (newClassroom != null) s.setClassroom(newClassroom);
//            }
//
//            int currentCount = subjectCounts.computeIfAbsent(classId, k -> new HashMap<>())
//                    .getOrDefault(subjectName, 0);
//            if (isDoubleSubject(s.getSchoolClass().getSpecialty(), subjectName) && currentCount >= 2) continue;
//            if (currentCount >= 1 && !isDoubleSubject(s.getSchoolClass().getSpecialty(), subjectName)) continue;
//
//            teacherSlots.get(timeKey).add(s.getTeacher().getId());
//            classroomSlots.get(timeKey).add(s.getClassroom().getId());
//            subjectCounts.get(classId).merge(subjectName, 1, Integer::sum);
//            repaired.add(s);
//        }
//
//        // Regenerate missing schedules
//        for (SchoolClass cls : classes) {
//            List<Subject> requiredSubjects = getSubjectsForSpecialty(cls.getSpecialty(), subjectRepository.findAll());
//            Map<String, Integer> requiredFreq = requiredSubjects.stream()
//                    .collect(Collectors.groupingBy(Subject::getName, Collectors.summingInt(s -> 1)));
//            Map<String, Integer> actualFreq = subjectCounts.getOrDefault(cls.getId(), new HashMap<>());
//
//            for (String subject : requiredFreq.keySet()) {
//                int required = requiredFreq.get(subject);
//                int actual = actualFreq.getOrDefault(subject, 0);
//                while (actual < required) {
//                    Schedule newSchedule = createSingleSchedule(cls, subject, repaired, teacherSlots, classroomSlots);
//                    if (newSchedule != null) {
//                        repaired.add(newSchedule);
//                        actual++;
//                        subjectCounts.computeIfAbsent(cls.getId(), k -> new HashMap<>())
//                                .merge(subject, 1, Integer::sum);
//                    } else {
//                        break;
//                    }
//                }
//            }
//        }
//        return repaired;
//    }
//
//    private Schedule createSingleSchedule(SchoolClass cls, String subjectName, List<Schedule> existing,
//                                          Map<String, Set<Long>> teacherSlots, Map<String, Set<Long>> classroomSlots) {
//        List<Teacher> teachers = teacherRepository.findAll().stream()
//                .filter(t -> t.getSubjects().stream().anyMatch(s -> s.getName().equals(subjectName)))
//                .collect(Collectors.toList());
//        List<Classroom> classrooms = classroomRepository.findAll();
//        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
//        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");
//        Random random = new Random();
//
//        Subject subject = subjectRepository.findAll().stream()
//                .filter(s -> s.getName().equals(subjectName)).findFirst().orElse(null);
//        if (subject == null || teachers.isEmpty()) return null;
//
//        for (int attempt = 0; attempt < 50; attempt++) {
//            String day = days.get(random.nextInt(days.size()));
//            String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
//            String timeKey = day + "-" + timeSlot;
//
//            if (existing.stream().anyMatch(s -> s.getSchoolClass().equals(cls) &&
//                    s.getDay().equals(day) && s.getTimeSlot().equals(timeSlot))) continue;
//            if (isDoubleSubject(cls.getSpecialty(), subjectName) && existing.stream()
//                    .filter(s -> s.getSchoolClass().equals(cls) && s.getSubject().equals(subject))
//                    .anyMatch(s -> s.getDay().equals(day))) continue;
//
//            Teacher teacher = teachers.get(random.nextInt(teachers.size()));
//            if (teacherSlots.getOrDefault(timeKey, new HashSet<>()).contains(teacher.getId())) continue;
//
//            Classroom classroom = findAvailableClassroom(timeKey, classrooms, existing);
//            if (classroom == null) continue;
//
//            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>()).add(teacher.getId());
//            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>()).add(classroom.getId());
//            return new Schedule(teacher, classroom, subject, cls, day, timeSlot);
//        }
//        return null;
//    }
//    private Teacher findAvailableTeacher(Subject subject, String timeKey, Map<String, Set<Long>> teacherSlots) {
//        return teacherRepository.findAll().stream()
//                .filter(t -> t.getSubjects().contains(subject))
//                .filter(t -> !teacherSlots.get(timeKey).contains(t.getId()))
//                .findFirst()
//                .orElse(null);
//    }
//
//    public List<List<Schedule>> runGeneticAlgorithm(int generations, int numClasses) {
//        List<SchoolClass> classes = schoolClassRepository.findAll();
//        if (classes.size() < numClasses) {
//            throw new IllegalStateException("Not enough classes: " + classes.size() + " < " + numClasses);
//        }
//        classes = classes.subList(0, numClasses); // Take first numClasses classes
//
//        List<List<Schedule>> population = initializePopulation(classes);
//
//        long startTime = System.currentTimeMillis(); // Timeout: Start timing
//        for (int gen = 0; gen < Math.max(generations, MAX_GENERATIONS) && (System.currentTimeMillis() - startTime) < 60_000; gen++) {
//            if (gen % 100 == 0) { // Diagnostic: Log every 100 generations
//                List<Schedule> bestSoFar = population.stream()
//                        .max(Comparator.comparingInt(this::calculateFitness))
//                        .orElse(Collections.emptyList());
//                logger.info("Generation {}: Best Fitness = {}", gen, calculateFitness(bestSoFar));
//            }
//
//            List<List<Schedule>> newPopulation = new ArrayList<>();
//            int elitismCount = (int)(POPULATION_SIZE * 0.1);
//
//            population.stream()
//                    .sorted(Comparator.comparingInt(this::calculateFitness).reversed())
//                    .limit(elitismCount)
//                    .forEach(newPopulation::add);
//
//            for (int i = elitismCount; i < POPULATION_SIZE; i++) {
//                List<Schedule> parent1 = selectParent(population);
//                List<Schedule> parent2 = selectParent(population);
//                List<Schedule> child = crossover(parent1, parent2);
//
//                if (new Random().nextDouble() < MUTATION_RATE) {
//                    mutate(child);
//                }
//                newPopulation.add(child);
//            }
//
//            population = newPopulation;
//
//            List<Schedule> best = population.stream()
//                    .max(Comparator.comparingInt(this::calculateFitness))
//                    .orElse(Collections.emptyList());
//            int maxScore = numClasses * SCHEDULES_PER_CLASS * 20; // e.g., 12 * 10 * 20 = 2400
//            if (calculateFitness(best) >= maxScore) {
//                logger.info("Optimal solution found at generation {}", gen + 1);
//                break;
//            }
//        }
//
//        List<Schedule> bestIndividual = population.stream()
//                .max(Comparator.comparingInt(this::calculateFitness))
//                .orElse(Collections.emptyList());
//
//        if (bestIndividual.isEmpty()) {
//            logger.error("No valid schedule generated.");
//            return Collections.emptyList();
//        }
//
//        bestIndividual = repairConflicts(bestIndividual, classes);
//        Map<Long, List<Schedule>> classSchedules = bestIndividual.stream()
//                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));
//        List<List<Schedule>> result = new ArrayList<>(Collections.nCopies(classes.size(), null));
//
//        for (SchoolClass schoolClass : classes) {
//            int index = classes.indexOf(schoolClass);
//            result.set(index, classSchedules.getOrDefault(schoolClass.getId(), Collections.emptyList()));
//        }
//
//        logger.info("Best fitness score: {}", calculateFitness(bestIndividual));
//        if ((System.currentTimeMillis() - startTime) >= 180_000) {
//            logger.warn("Algorithm terminated due to  timeout.");
//        }
//        return result;
//    }
//}
//




package tn.esprit.timetableservice.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.timetableservice.entities.*;
import tn.esprit.timetableservice.repositories.ClassroomRepository;
import tn.esprit.timetableservice.repositories.SchoolClassRepository;
import tn.esprit.timetableservice.repositories.SubjectRepository;
import tn.esprit.timetableservice.repositories.TeacherRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final int POPULATION_SIZE = 6000;
    private static final double MUTATION_RATE = 0.2;
    private static final int SCHEDULES_PER_CLASS = 10; // 10 slots per class
    private static final int MAX_GENERATIONS = 3000;
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired private TeacherRepository teacherRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private SchoolClassRepository schoolClassRepository;

    private List<Schedule> createRandomScheduleForClass(SchoolClass schoolClass) {
        List<Schedule> schedule = new ArrayList<>();
        List<Teacher> teachers = teacherRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Subject> subjects = subjectRepository.findAll();
        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");

        if (teachers.isEmpty() || classrooms.isEmpty() || subjects.isEmpty()) {
            throw new IllegalStateException("Cannot create schedule: Teachers, Classrooms, or Subjects list is empty.");
        }

        Random random = new Random();
        List<Subject> subjectsToSchedule = getSubjectsForSpecialty(schoolClass.getSpecialty(), subjects);
        Collections.shuffle(subjectsToSchedule);

        Map<String, Teacher> subjectTeacherMap = new HashMap<>();
        Map<String, Set<String>> scheduledTimes = new HashMap<>();

        for (Subject subject : subjectsToSchedule) {
            String subjectName = subject.getName();
            boolean isDoubleSubject = isDoubleSubject(schoolClass.getSpecialty(), subjectName);

            Teacher teacher = subjectTeacherMap.get(subjectName);
            if (teacher == null) {
                List<Teacher> eligibleTeachers = teachers.stream()
                        .filter(t -> t.getSubjects().contains(subject))
                        .collect(Collectors.toList());
                if (eligibleTeachers.isEmpty()) {
                    logger.warn("No teacher available for subject: {}", subjectName);
                    continue;
                }
                teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
                subjectTeacherMap.put(subjectName, teacher);
            }

            for (int attempt = 0; attempt < 100; attempt++) {
                String day = days.get(random.nextInt(days.size()));
                String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
                String timeKey = day + "-" + timeSlot;

                if (scheduledTimes.containsKey(timeKey)) continue;
                if (isDoubleSubject && schedule.stream()
                        .filter(s -> s.getSubject().equals(subject))
                        .anyMatch(s -> s.getDay().equals(day))) continue;

                Classroom classroom = findAvailableClassroom(timeKey, classrooms, schedule);
                if (classroom == null) {
                    logger.debug("No classroom available for {} at {}", subjectName, timeKey);
                    continue;
                }

                Schedule newSchedule = new Schedule(teacher, classroom, subject, schoolClass, day, timeSlot);
                schedule.add(newSchedule);
                scheduledTimes.put(timeKey, new HashSet<>());
                break;
            }
        }
        if (schedule.size() < SCHEDULES_PER_CLASS) {
            logger.warn("Generated only {} slots for class {}, expected {}", schedule.size(), schoolClass.getName(), SCHEDULES_PER_CLASS);
        }
        return schedule;
    }

    private List<Subject> getSubjectsForSpecialty(String specialty, List<Subject> allSubjects) {
        List<Subject> subjectsToSchedule = new ArrayList<>();
        Subject math = allSubjects.stream().filter(s -> s.getName().equals("Math")).findFirst().orElse(null);
        Subject physics = allSubjects.stream().filter(s -> s.getName().equals("Physics")).findFirst().orElse(null);
        Subject english = allSubjects.stream().filter(s -> s.getName().equals("English")).findFirst().orElse(null);
        Subject history = allSubjects.stream().filter(s -> s.getName().equals("History")).findFirst().orElse(null);
        Subject chemistry = allSubjects.stream().filter(s -> s.getName().equals("Chemistry")).findFirst().orElse(null);
        Subject french = allSubjects.stream().filter(s -> s.getName().equals("French")).findFirst().orElse(null);
        Subject geography = allSubjects.stream().filter(s -> s.getName().equals("Geography")).findFirst().orElse(null);
        Subject biology = allSubjects.stream().filter(s -> s.getName().equals("Biology")).findFirst().orElse(null);
        Subject philosophy = allSubjects.stream().filter(s -> s.getName().equals("Philosophy")).findFirst().orElse(null);
        Subject technology = allSubjects.stream().filter(s -> s.getName().equals("Technology")).findFirst().orElse(null);

        switch (specialty) {
            case "Math":
                if (math != null) { subjectsToSchedule.add(math); subjectsToSchedule.add(math); } // Math twice
                if (physics != null) { subjectsToSchedule.add(physics); subjectsToSchedule.add(physics); } // Physics twice
                if (english != null) subjectsToSchedule.add(english);
                if (history != null) subjectsToSchedule.add(history);
                if (chemistry != null) subjectsToSchedule.add(chemistry);
                if (french != null) subjectsToSchedule.add(french);
                if (geography != null) subjectsToSchedule.add(geography);
                if (biology != null) subjectsToSchedule.add(biology);
                break;
            case "Lettre":
                if (philosophy != null) { subjectsToSchedule.add(philosophy); subjectsToSchedule.add(philosophy); } // Philosophy twice
                if (french != null) { subjectsToSchedule.add(french); subjectsToSchedule.add(french); } // French twice
                if (english != null) subjectsToSchedule.add(english);
                if (history != null) subjectsToSchedule.add(history);
                if (chemistry != null) subjectsToSchedule.add(chemistry);
                if (geography != null) subjectsToSchedule.add(geography);
                if (math != null) subjectsToSchedule.add(math);
                if (physics != null) subjectsToSchedule.add(physics);
                break;
            case "Technology":
                if (technology != null) { subjectsToSchedule.add(technology); subjectsToSchedule.add(technology); } // Technology twice
                if (math != null) { subjectsToSchedule.add(math); subjectsToSchedule.add(math); } // Math twice
                if (english != null) subjectsToSchedule.add(english);
                if (history != null) subjectsToSchedule.add(history);
                if (chemistry != null) subjectsToSchedule.add(chemistry);
                if (french != null) subjectsToSchedule.add(french);
                if (geography != null) subjectsToSchedule.add(geography);
                if (physics != null) subjectsToSchedule.add(physics);
                break;
            default:
                throw new IllegalArgumentException("Unknown specialty: " + specialty);
        }
        return subjectsToSchedule;
    }

    private boolean isDoubleSubject(String specialty, String subjectName) {
        switch (specialty) {
            case "Math":
                return "Math".equals(subjectName) || "Physics".equals(subjectName);
            case "Lettre":
                return "Philosophy".equals(subjectName) || "French".equals(subjectName);
            case "Technology":
                return "Technology".equals(subjectName) || "Math".equals(subjectName);
            default:
                return false;
        }
    }

    private Classroom findAvailableClassroom(String timeKey, List<Classroom> classrooms, List<Schedule> existing) {
        Set<Long> usedClassrooms = existing.stream()
                .filter(s -> (s.getDay() + "-" + s.getTimeSlot()).equals(timeKey))
                .map(s -> s.getClassroom().getId())
                .collect(Collectors.toSet());

        return classrooms.stream()
                .filter(c -> !usedClassrooms.contains(c.getId()))
                .findFirst()
                .orElse(null);
    }

    private List<List<Schedule>> initializePopulation(List<SchoolClass> classes) {
        List<List<Schedule>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<Schedule> individual = new ArrayList<>();
            for (SchoolClass schoolClass : classes) {
                individual.addAll(createRandomScheduleForClass(schoolClass));
            }
            population.add(individual);
        }
        return population;
    }

    public int calculateFitness(List<Schedule> individual) {
        int score = 0;
        Map<String, Set<Long>> teacherSlots = new HashMap<>();
        Map<String, Set<Long>> classroomSlots = new HashMap<>();
        Map<Long, Map<String, Set<String>>> classTimeSlots = new HashMap<>();
        Map<Long, Map<String, Integer>> classSubjectFrequency = new HashMap<>();
        Map<Long, Map<String, Map<String, Integer>>> classSubjectDayFrequency = new HashMap<>();
        Map<Long, Map<String, Set<Long>>> classSubjectTeachers = new HashMap<>();

        for (Schedule s : individual) {
            String timeKey = s.getDay() + "-" + s.getTimeSlot();
            Long classId = s.getSchoolClass().getId();
            String subjectName = s.getSubject().getName();
            String specialty = s.getSchoolClass().getSpecialty();
            Long teacherId = s.getTeacher().getId();

            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>());

            if (!teacherSlots.get(timeKey).add(teacherId)) score -= 100; // Teacher conflict
            else score += 10; // Reward for no conflict

            if (!classroomSlots.get(timeKey).add(s.getClassroom().getId())) score -= 100; // Classroom conflict
            else score += 10;

            classTimeSlots.computeIfAbsent(classId, k -> new HashMap<>())
                    .computeIfAbsent(timeKey, k -> new HashSet<>())
                    .add(subjectName);

            if (classTimeSlots.get(classId).get(timeKey).size() > 1) score -= 500; // Class overlap

            classSubjectFrequency.computeIfAbsent(classId, k -> new HashMap<>())
                    .merge(subjectName, 1, Integer::sum);
            classSubjectDayFrequency.computeIfAbsent(classId, k -> new HashMap<>())
                    .computeIfAbsent(subjectName, k -> new HashMap<>())
                    .merge(s.getDay(), 1, Integer::sum);

            if (isDoubleSubject(specialty, subjectName)) {
                classSubjectTeachers.computeIfAbsent(classId, k -> new HashMap<>())
                        .computeIfAbsent(subjectName, k -> new HashSet<>())
                        .add(teacherId);
            }
        }

        for (Long classId : classSubjectFrequency.keySet()) {
            String specialty = individual.stream()
                    .filter(s -> s.getSchoolClass().getId().equals(classId))
                    .findFirst().get().getSchoolClass().getSpecialty();
            Map<String, Integer> freq = classSubjectFrequency.get(classId);
            Map<String, Map<String, Integer>> dayFreq = classSubjectDayFrequency.get(classId);
            Map<String, Set<Long>> teachers = classSubjectTeachers.getOrDefault(classId, new HashMap<>());

            for (String subject : freq.keySet()) {
                int frequency = freq.get(subject);
                if (isDoubleSubject(specialty, subject)) {
                    if (frequency == 2) score += 50; // Reward correct double subject
                    else if (frequency > 2) score -= 500; // Too many
                    if (dayFreq.get(subject).values().stream().anyMatch(c -> c > 1)) score -= 500; // Same day
                    if (frequency >= 2 && teachers.get(subject).size() > 1) score -= 500; // Different teachers
                } else if (frequency > 1) {
                    score -= 500; // Non-double subject repeated
                }
            }
        }

        return score;
    }

    private List<Schedule> selectParent(List<List<Schedule>> population) {
        Random random = new Random();
        List<List<Schedule>> tournament = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tournament.add(population.get(random.nextInt(population.size())));
        }
        return tournament.stream()
                .max(Comparator.comparingInt(this::calculateFitness))
                .orElse(null);
    }

    private List<Schedule> crossover(List<Schedule> parent1, List<Schedule> parent2) {
        if (parent1 == null || parent2 == null) return new ArrayList<>();

        Map<Long, List<Schedule>> parent1Classes = parent1.stream()
                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));
        Map<Long, List<Schedule>> parent2Classes = parent2.stream()
                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));

        List<SchoolClass> classes = schoolClassRepository.findAll();
        List<Schedule> child = new ArrayList<>();
        Random random = new Random();

        for (Long classId : parent1Classes.keySet()) {
            if (random.nextBoolean()) {
                child.addAll(parent1Classes.get(classId));
            } else {
                child.addAll(parent2Classes.getOrDefault(classId, Collections.emptyList()));
            }
        }
        return repairConflicts(child, classes);
    }

    private void mutate(List<Schedule> schedule) {
        if (schedule.isEmpty()) return;

        Random random = new Random();
        Schedule toMutate = schedule.get(random.nextInt(schedule.size()));
        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");

        for (int attempt = 0; attempt < 50; attempt++) {
            String newDay = days.get(random.nextInt(days.size()));
            String newTime = timeSlots.get(random.nextInt(timeSlots.size()));

            boolean conflict = schedule.stream()
                    .anyMatch(s -> s != toMutate &&
                            s.getDay().equals(newDay) &&
                            s.getTimeSlot().equals(newTime) &&
                            (s.getTeacher().equals(toMutate.getTeacher()) ||
                                    s.getClassroom().equals(toMutate.getClassroom())));

            if (!conflict) {
                toMutate.setDay(newDay);
                toMutate.setTimeSlot(newTime);
                break;
            }
        }
    }

    private List<Schedule> repairConflicts(List<Schedule> individual, List<SchoolClass> classes) {
        List<Schedule> repaired = new ArrayList<>();
        Map<String, Set<Long>> teacherSlots = new HashMap<>();
        Map<String, Set<Long>> classroomSlots = new HashMap<>();
        Map<Long, Map<String, Integer>> subjectCounts = new HashMap<>();
        List<Teacher> allTeachers = teacherRepository.findAll();
        List<Classroom> allClassrooms = classroomRepository.findAll();

        for (Schedule s : individual) {
            String timeKey = s.getDay() + "-" + s.getTimeSlot();
            Long classId = s.getSchoolClass().getId();
            String subjectName = s.getSubject().getName();

            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>());

            if (teacherSlots.get(timeKey).contains(s.getTeacher().getId())) {
                Teacher newTeacher = findAvailableTeacher(s.getSubject(), timeKey, teacherSlots);
                if (newTeacher != null) s.setTeacher(newTeacher);
            }

            if (classroomSlots.get(timeKey).contains(s.getClassroom().getId())) {
                Classroom newClassroom = findAvailableClassroom(timeKey, allClassrooms, repaired);
                if (newClassroom != null) s.setClassroom(newClassroom);
            }

            int currentCount = subjectCounts.computeIfAbsent(classId, k -> new HashMap<>())
                    .getOrDefault(subjectName, 0);
            if (isDoubleSubject(s.getSchoolClass().getSpecialty(), subjectName) && currentCount >= 2) continue;
            if (currentCount >= 1 && !isDoubleSubject(s.getSchoolClass().getSpecialty(), subjectName)) continue;

            teacherSlots.get(timeKey).add(s.getTeacher().getId());
            classroomSlots.get(timeKey).add(s.getClassroom().getId());
            subjectCounts.get(classId).merge(subjectName, 1, Integer::sum);
            repaired.add(s);
        }

        // Regenerate missing schedules
        for (SchoolClass cls : classes) {
            List<Subject> requiredSubjects = getSubjectsForSpecialty(cls.getSpecialty(), subjectRepository.findAll());
            Map<String, Integer> requiredFreq = requiredSubjects.stream()
                    .collect(Collectors.groupingBy(Subject::getName, Collectors.summingInt(s -> 1)));
            Map<String, Integer> actualFreq = subjectCounts.getOrDefault(cls.getId(), new HashMap<>());

            for (String subject : requiredFreq.keySet()) {
                int required = requiredFreq.get(subject);
                int actual = actualFreq.getOrDefault(subject, 0);
                while (actual < required) {
                    Schedule newSchedule = createSingleSchedule(cls, subject, repaired, teacherSlots, classroomSlots);
                    if (newSchedule != null) {
                        repaired.add(newSchedule);
                        actual++;
                        subjectCounts.computeIfAbsent(cls.getId(), k -> new HashMap<>())
                                .merge(subject, 1, Integer::sum);
                    } else {
                        break;
                    }
                }
            }
        }
        return repaired;
    }

    private Schedule createSingleSchedule(SchoolClass cls, String subjectName, List<Schedule> existing,
                                          Map<String, Set<Long>> teacherSlots, Map<String, Set<Long>> classroomSlots) {
        List<Teacher> teachers = teacherRepository.findAll().stream()
                .filter(t -> t.getSubjects().stream().anyMatch(s -> s.getName().equals(subjectName)))
                .collect(Collectors.toList());
        List<Classroom> classrooms = classroomRepository.findAll();
        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");
        Random random = new Random();

        Subject subject = subjectRepository.findAll().stream()
                .filter(s -> s.getName().equals(subjectName)).findFirst().orElse(null);
        if (subject == null || teachers.isEmpty()) return null;

        for (int attempt = 0; attempt < 50; attempt++) {
            String day = days.get(random.nextInt(days.size()));
            String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
            String timeKey = day + "-" + timeSlot;

            if (existing.stream().anyMatch(s -> s.getSchoolClass().equals(cls) &&
                    s.getDay().equals(day) && s.getTimeSlot().equals(timeSlot))) continue;
            if (isDoubleSubject(cls.getSpecialty(), subjectName) && existing.stream()
                    .filter(s -> s.getSchoolClass().equals(cls) && s.getSubject().equals(subject))
                    .anyMatch(s -> s.getDay().equals(day))) continue;

            Teacher teacher = teachers.get(random.nextInt(teachers.size()));
            if (teacherSlots.getOrDefault(timeKey, new HashSet<>()).contains(teacher.getId())) continue;

            Classroom classroom = findAvailableClassroom(timeKey, classrooms, existing);
            if (classroom == null) continue;

            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>()).add(teacher.getId());
            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>()).add(classroom.getId());
            return new Schedule(teacher, classroom, subject, cls, day, timeSlot);
        }
        return null;
    }
    private Teacher findAvailableTeacher(Subject subject, String timeKey, Map<String, Set<Long>> teacherSlots) {
        return teacherRepository.findAll().stream()
                .filter(t -> t.getSubjects().contains(subject))
                .filter(t -> !teacherSlots.get(timeKey).contains(t.getId()))
                .findFirst()
                .orElse(null);
    }

    public List<List<Schedule>> runGeneticAlgorithm(int generations, int numClasses) {
        List<SchoolClass> classes = schoolClassRepository.findAll();
        if (classes.size() < numClasses) {
            throw new IllegalStateException("Not enough classes: " + classes.size() + " < " + numClasses);
        }
        classes = classes.subList(0, numClasses); // Take first numClasses classes

        List<List<Schedule>> population = initializePopulation(classes);

        long startTime = System.currentTimeMillis(); // Timeout: Start timing
        for (int gen = 0; gen < Math.max(generations, MAX_GENERATIONS) && (System.currentTimeMillis() - startTime) < 60_000; gen++) {
            if (gen % 100 == 0) { // Diagnostic: Log every 100 generations
                List<Schedule> bestSoFar = population.stream()
                        .max(Comparator.comparingInt(this::calculateFitness))
                        .orElse(Collections.emptyList());
                logger.info("Generation {}: Best Fitness = {}", gen, calculateFitness(bestSoFar));
            }

            List<List<Schedule>> newPopulation = new ArrayList<>();
            int elitismCount = (int)(POPULATION_SIZE * 0.1);

            population.stream()
                    .sorted(Comparator.comparingInt(this::calculateFitness).reversed())
                    .limit(elitismCount)
                    .forEach(newPopulation::add);

            for (int i = elitismCount; i < POPULATION_SIZE; i++) {
                List<Schedule> parent1 = selectParent(population);
                List<Schedule> parent2 = selectParent(population);
                List<Schedule> child = crossover(parent1, parent2);

                if (new Random().nextDouble() < MUTATION_RATE) {
                    mutate(child);
                }
                newPopulation.add(child);
            }

            population = newPopulation;

            List<Schedule> best = population.stream()
                    .max(Comparator.comparingInt(this::calculateFitness))
                    .orElse(Collections.emptyList());
            int maxScore = numClasses * SCHEDULES_PER_CLASS * 25;
            if (calculateFitness(best) >= maxScore) {
                logger.info("Optimal solution found at generation {}", gen + 1);
                break;
            }
        }

        List<Schedule> bestIndividual = population.stream()
                .max(Comparator.comparingInt(this::calculateFitness))
                .orElse(Collections.emptyList());

        if (bestIndividual.isEmpty()) {
            logger.error("No valid schedule generated.");
            return Collections.emptyList();
        }

        bestIndividual = repairConflicts(bestIndividual, classes);
        Map<Long, List<Schedule>> classSchedules = bestIndividual.stream()
                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));
        List<List<Schedule>> result = new ArrayList<>(Collections.nCopies(classes.size(), null));

        for (SchoolClass schoolClass : classes) {
            int index = classes.indexOf(schoolClass);
            result.set(index, classSchedules.getOrDefault(schoolClass.getId(), Collections.emptyList()));
        }

        logger.info("Best fitness score: {}", calculateFitness(bestIndividual));
        if ((System.currentTimeMillis() - startTime) >= 180_000) {
            logger.warn("Algorithm terminated due to  timeout.");
        }
        return result;
    }
}

