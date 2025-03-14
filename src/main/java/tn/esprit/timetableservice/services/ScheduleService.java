//
//package tn.esprit.timetableservice.services;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import tn.esprit.timetableservice.entities.Classroom;
//import tn.esprit.timetableservice.entities.Schedule;
//import tn.esprit.timetableservice.entities.SchoolClass;
//import tn.esprit.timetableservice.entities.Subject;
//import tn.esprit.timetableservice.entities.Teacher;
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
//    private static final int POPULATION_SIZE = 800;
//    private static final double MUTATION_RATE = 0.1;
//    private static final int SCHEDULES_PER_CLASS = 10;
//    private static final int MAX_GENERATIONS = 1500;
//    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
//
//    @Autowired
//    private TeacherRepository teacherRepository;
//
//    @Autowired
//    private ClassroomRepository classroomRepository;
//
//    @Autowired
//    private SubjectRepository subjectRepository;
//
//    @Autowired
//    private SchoolClassRepository schoolClassRepository;
//
//    /**
//     * Generate a random schedule for a specific class with constraints:
//     * - No two schedules for the same class can share the same day and time slot.
//     * - Only Math and Physics can be scheduled up to twice a week, never twice on the same day.
//     * - Other subjects can be scheduled only once a week.
//     * - Same teacher for a subject scheduled twice a week for the same class.
//     */
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
//        int attempts = 0;
//        int maxAttempts = SCHEDULES_PER_CLASS * 3;
//
//        Map<String, Integer> subjectFrequency = new HashMap<>();
//        Map<String, Map<String, Integer>> subjectDayFrequency = new HashMap<>();
//        Map<Subject, Teacher> subjectTeacherMap = new HashMap<>(); // Ensures same teacher for same subject
//
//        while (schedule.size() < SCHEDULES_PER_CLASS && attempts < maxAttempts) {
//            Subject subject = subjects.get(random.nextInt(subjects.size()));
//            String subjectName = subject.getName();
//
//            boolean isMathOrPhysics = "Math".equals(subjectName) || "Physics".equals(subjectName);
//            int currentFrequency = subjectFrequency.getOrDefault(subjectName, 0);
//            Map<String, Integer> dayFreq = subjectDayFrequency.computeIfAbsent(subjectName, k -> new HashMap<>());
//
//            if (isMathOrPhysics) {
//                if (currentFrequency >= 2) {
//                    logger.debug("Max frequency (2) reached for subject: {}", subjectName);
//                    attempts++;
//                    continue;
//                }
//            } else {
//                if (currentFrequency >= 1) {
//                    logger.debug("Max frequency (1) reached for subject: {}", subjectName);
//                    attempts++;
//                    continue;
//                }
//            }
//
//            String day = days.get(random.nextInt(days.size()));
//            if (isMathOrPhysics && dayFreq.getOrDefault(day, 0) >= 1) {
//                logger.debug("Cannot schedule {} again on the same day: {}", subjectName, day);
//                attempts++;
//                continue;
//            }
//
//            String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
//            String timeKey = day + "-" + timeSlot;
//
//            boolean timeConflict = schedule.stream()
//                    .anyMatch(s -> s.getDay().equals(day) && s.getTimeSlot().equals(timeSlot));
//            if (timeConflict) {
//                logger.debug("Time slot conflict detected for day {} and time slot {}", day, timeSlot);
//                attempts++;
//                continue;
//            }
//
//            Teacher teacher;
//            if (subjectTeacherMap.containsKey(subject)) {
//                teacher = subjectTeacherMap.get(subject); // Use same teacher if subject already scheduled
//            } else {
//                List<Teacher> eligibleTeachers = teachers.stream()
//                        .filter(t -> t.getSubjects().contains(subject))
//                        .collect(Collectors.toList());
//                if (eligibleTeachers.isEmpty()) {
//                    logger.warn("No teacher available for subject: {}", subjectName);
//                    attempts++;
//                    continue;
//                }
//                teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
//                subjectTeacherMap.put(subject, teacher); // Record teacher for this subject
//            }
//
//            Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
//
//            schedule.add(new Schedule(teacher, classroom, subject, schoolClass, day, timeSlot));
//            subjectFrequency.merge(subjectName, 1, Integer::sum);
//            dayFreq.merge(day, 1, Integer::sum);
//            attempts++;
//        }
//
//        if (schedule.size() < SCHEDULES_PER_CLASS) {
//            logger.warn("Generated only {} schedules for class {} out of {}. Attempting to fill with partial schedules.",
//                    schedule.size(), schoolClass.getName(), SCHEDULES_PER_CLASS);
//            while (schedule.size() < SCHEDULES_PER_CLASS && attempts < maxAttempts * 2) {
//                Subject subject = subjects.get(random.nextInt(subjects.size()));
//                String subjectName = subject.getName();
//
//                Teacher teacher;
//                if (subjectTeacherMap.containsKey(subject)) {
//                    teacher = subjectTeacherMap.get(subject);
//                } else {
//                    List<Teacher> eligibleTeachers = teachers.stream()
//                            .filter(t -> t.getSubjects().contains(subject))
//                            .collect(Collectors.toList());
//                    if (eligibleTeachers.isEmpty()) {
//                        attempts++;
//                        continue;
//                    }
//                    teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
//                    subjectTeacherMap.put(subject, teacher);
//                }
//
//                Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
//                String day = days.get(random.nextInt(days.size()));
//                String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
//                String timeKey = day + "-" + timeSlot;
//
//                boolean timeConflict = schedule.stream()
//                        .anyMatch(s -> s.getDay().equals(day) && s.getTimeSlot().equals(timeSlot));
//                if (timeConflict) {
//                    attempts++;
//                    continue;
//                }
//
//                boolean isMathOrPhysics = "Math".equals(subjectName) || "Physics".equals(subjectName);
//                Map<String, Integer> dayFreqFallback = subjectDayFrequency.computeIfAbsent(subjectName, k -> new HashMap<>());
//                if (isMathOrPhysics && dayFreqFallback.getOrDefault(day, 0) >= 1) {
//                    attempts++;
//                    continue;
//                }
//
//                schedule.add(new Schedule(teacher, classroom, subject, schoolClass, day, timeSlot));
//                subjectFrequency.merge(subjectName, 1, Integer::sum);
//                dayFreqFallback.merge(day, 1, Integer::sum);
//                attempts++;
//            }
//        }
//
//        return schedule;
//    }
//
//    /**
//     * Initialize population with schedules for all classes
//     */
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
//    /**
//     * Calculate fitness, penalizing teacher and classroom overlaps, and enforcing subject frequency rules
//     */
//    public int calculateFitness(List<Schedule> individual) {
//        int score = 0;
//        Map<String, Set<Long>> teacherSlots = new HashMap<>();
//        Map<String, Set<Long>> classroomSlots = new HashMap<>();
//        Map<Long, Map<String, Set<String>>> classTimeSlots = new HashMap<>();
//        Map<Long, Map<String, Integer>> classSubjectFrequency = new HashMap<>();
//        Map<Long, Map<String, Map<String, Integer>>> classSubjectDayFrequency = new HashMap<>();
//
//        for (Schedule s : individual) {
//            String timeKey = s.getDay() + "-" + s.getTimeSlot();
//            Long classId = s.getSchoolClass().getId();
//            String subjectName = s.getSubject().getName();
//
//            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//
//            if (!teacherSlots.get(timeKey).add(s.getTeacher().getId())) {
//                score -= 100;
//            } else {
//                score += 10;
//            }
//
//            if (!classroomSlots.get(timeKey).add(s.getClassroom().getId())) {
//                score -= 100;
//            } else {
//                score += 10;
//            }
//
//            classTimeSlots.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(timeKey, k -> new HashSet<>())
//                    .add(subjectName);
//
//            if (classTimeSlots.get(classId).get(timeKey).size() > 1) {
//                score -= 2000;
//            }
//
//            classSubjectFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                    .merge(subjectName, 1, Integer::sum);
//            classSubjectDayFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(subjectName, k -> new HashMap<>())
//                    .merge(s.getDay(), 1, Integer::sum);
//
//            if ("Math".equals(subjectName) || "Physics".equals(subjectName)) {
//                int frequency = classSubjectFrequency.get(classId).get(subjectName);
//                Map<String, Integer> dayFreq = classSubjectDayFrequency.get(classId).get(subjectName);
//                if (frequency > 2) {
//                    score -= 1000;
//                }
//                for (int count : dayFreq.values()) {
//                    if (count > 1) {
//                        score -= 1000;
//                    }
//                }
//            } else {
//                if (classSubjectFrequency.get(classId).get(subjectName) > 1) {
//                    score -= 1000;
//                }
//            }
//        }
//        return score;
//    }
//
//    /**
//     * Select the fittest parent using tournament selection
//     */
//    private List<Schedule> selectParent(List<List<Schedule>> population) {
//        Random random = new Random();
//        List<List<Schedule>> tournament = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            tournament.add(population.get(random.nextInt(population.size())));
//        }
//        tournament.sort(Comparator.comparingInt(this::calculateFitness).reversed());
//        return tournament.get(0);
//    }
//
//    /**
//     * Perform crossover between two parents, ensuring teacher consistency for subjects scheduled twice
//     */
//    private List<Schedule> crossover(List<Schedule> parent1, List<Schedule> parent2) {
//        if (parent1.isEmpty() || parent2.isEmpty()) {
//            logger.warn("Crossover skipped: parent1 size {}, parent2 size {}", parent1.size(), parent2.size());
//            return new ArrayList<>(parent1.isEmpty() ? parent2 : parent1);
//        }
//        Random random = new Random();
//        int crossoverPoint = random.nextInt(Math.min(parent1.size(), parent2.size()));
//
//        List<Schedule> child = new ArrayList<>();
//        child.addAll(parent1.subList(0, crossoverPoint));
//        child.addAll(parent2.subList(crossoverPoint, parent2.size()));
//
//        // Enforce teacher consistency per subject per class
//        Map<Long, Map<Subject, List<Schedule>>> classSubjectSchedules = new HashMap<>();
//        for (Schedule s : child) {
//            Long classId = s.getSchoolClass().getId();
//            Subject subject = s.getSubject();
//            classSubjectSchedules
//                    .computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(subject, k -> new ArrayList<>())
//                    .add(s);
//        }
//        for (Map<Subject, List<Schedule>> subjectMap : classSubjectSchedules.values()) {
//            for (List<Schedule> schedules : subjectMap.values()) {
//                if (schedules.size() > 1) {
//                    Teacher selectedTeacher = schedules.stream()
//                            .map(Schedule::getTeacher)
//                            .min(Comparator.comparingLong(Teacher::getId))
//                            .orElse(null);
//                    if (selectedTeacher != null) {
//                        for (Schedule s : schedules) {
//                            s.setTeacher(selectedTeacher);
//                        }
//                    }
//                }
//            }
//        }
//
//        // Check for constraints and remove violations
//        Map<Long, Map<String, Set<String>>> classTimeSlots = new HashMap<>();
//        Map<Long, Map<String, Integer>> subjectFrequency = new HashMap<>();
//        Map<Long, Map<String, Map<String, Integer>>> subjectDayFrequency = new HashMap<>();
//        List<Schedule> schedulesToRemove = new ArrayList<>();
//
//        for (Schedule s : child) {
//            Long classId = s.getSchoolClass().getId();
//            String subjectName = s.getSubject().getName();
//            String timeKey = s.getDay() + "-" + s.getTimeSlot();
//
//            if (!s.getTeacher().getSubjects().contains(s.getSubject())) {
//                List<Teacher> eligibleTeachers = teacherRepository.findAll().stream()
//                        .filter(t -> t.getSubjects().contains(s.getSubject()))
//                        .collect(Collectors.toList());
//                if (!eligibleTeachers.isEmpty()) {
//                    s.setTeacher(eligibleTeachers.get(random.nextInt(eligibleTeachers.size())));
//                }
//            }
//
//            classTimeSlots.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(timeKey, k -> new HashSet<>())
//                    .add(subjectName);
//
//            subjectFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                    .merge(subjectName, 1, Integer::sum);
//            subjectDayFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(subjectName, k -> new HashMap<>())
//                    .merge(s.getDay(), 1, Integer::sum);
//        }
//
//        for (Schedule s : child) {
//            Long classId = s.getSchoolClass().getId();
//            String subjectName = s.getSubject().getName();
//            String timeKey = s.getDay() + "-" + s.getTimeSlot();
//
//            if (classTimeSlots.get(classId).get(timeKey).size() > 1) {
//                schedulesToRemove.add(s);
//            }
//
//            int freq = subjectFrequency.get(classId).get(subjectName);
//            Map<String, Integer> dayFreq = subjectDayFrequency.get(classId).get(subjectName);
//
//            if ("Math".equals(subjectName) || "Physics".equals(subjectName)) {
//                if (freq > 2) {
//                    schedulesToRemove.add(s);
//                }
//                for (int count : dayFreq.values()) {
//                    if (count > 1) {
//                        schedulesToRemove.add(s);
//                    }
//                }
//            } else {
//                if (freq > 1) {
//                    schedulesToRemove.add(s);
//                }
//            }
//        }
//
//        child.removeAll(schedulesToRemove);
//        return child;
//    }
//
//    /**
//     * Mutate by swapping time slots, respecting subject constraints and no overlaps
//     */
//    private void mutate(List<Schedule> schedule) {
//        if (schedule.size() < 2) return;
//        Random random = new Random();
//        int index1 = random.nextInt(schedule.size());
//        int index2 = random.nextInt(schedule.size());
//
//        if (index1 != index2) {
//            Schedule s1 = schedule.get(index1);
//            Schedule s2 = schedule.get(index2);
//            String tempTimeSlot = s1.getTimeSlot();
//            String tempDay = s1.getDay();
//
//            s1.setTimeSlot(s2.getTimeSlot());
//            s1.setDay(s2.getDay());
//            s2.setTimeSlot(tempTimeSlot);
//            s2.setDay(tempDay);
//
//            Map<Long, Map<String, Set<String>>> classTimeSlots = new HashMap<>();
//            Map<Long, Map<String, Integer>> subjectFrequency = new HashMap<>();
//            Map<Long, Map<String, Map<String, Integer>>> subjectDays = new HashMap<>();
//            boolean validSwap = true;
//
//            for (Schedule s : schedule) {
//                Long classId = s.getSchoolClass().getId();
//                String subjectName = s.getSubject().getName();
//                String timeKey = s.getDay() + "-" + s.getTimeSlot();
//
//                classTimeSlots.computeIfAbsent(classId, k -> new HashMap<>())
//                        .computeIfAbsent(timeKey, k -> new HashSet<>())
//                        .add(subjectName);
//
//                if (classTimeSlots.get(classId).get(timeKey).size() > 1) {
//                    validSwap = false;
//                    break;
//                }
//
//                subjectFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                        .merge(subjectName, 1, Integer::sum);
//                subjectDays.computeIfAbsent(classId, k -> new HashMap<>())
//                        .computeIfAbsent(subjectName, k -> new HashMap<>())
//                        .merge(s.getDay(), 1, Integer::sum);
//
//                if ("Math".equals(subjectName) || "Physics".equals(subjectName)) {
//                    if (subjectFrequency.get(classId).get(subjectName) > 2) {
//                        validSwap = false;
//                        break;
//                    }
//                    Map<String, Integer> dayFreq = subjectDays.get(classId).get(subjectName);
//                    for (int count : dayFreq.values()) {
//                        if (count > 1) {
//                            validSwap = false;
//                            break;
//                        }
//                    }
//                } else {
//                    if (subjectFrequency.get(classId).get(subjectName) > 1) {
//                        validSwap = false;
//                        break;
//                    }
//                }
//            }
//
//            if (!validSwap) {
//                s1.setTimeSlot(tempTimeSlot);
//                s1.setDay(tempDay);
//                s2.setTimeSlot(s2.getTimeSlot());
//                s2.setDay(s2.getDay());
//            }
//        }
//    }
//
//    /**
//     * Run genetic algorithm to generate conflict-free timetables
//     */
//    public List<List<Schedule>> runGeneticAlgorithm(int generations, int numClasses) {
//        List<SchoolClass> classes = schoolClassRepository.findAll();
//        if (classes.size() < numClasses) {
//            throw new IllegalStateException("Not enough classes in database: " + classes.size() + " < " + numClasses);
//        }
//        classes = classes.subList(0, numClasses);
//
//        List<List<Schedule>> population = initializePopulation(classes);
//
//        for (int gen = 0; gen < Math.max(generations, MAX_GENERATIONS); gen++) {
//            List<List<Schedule>> newPopulation = new ArrayList<>();
//
//            for (int i = 0; i < population.size(); i++) {
//                List<Schedule> parent1 = selectParent(population);
//                List<Schedule> parent2 = selectParent(population);
//                List<Schedule> child = crossover(parent1, parent2);
//
//                if (new Random().nextDouble() < MUTATION_RATE) {
//                    mutate(child);
//                }
//
//                newPopulation.add(child);
//            }
//
//            population = newPopulation;
//
//            List<Schedule> best = population.stream()
//                    .max(Comparator.comparingInt(this::calculateFitness))
//                    .orElse(Collections.emptyList());
//            int maxPossibleScore = numClasses * SCHEDULES_PER_CLASS * 10;
//            if (calculateFitness(best) == maxPossibleScore) {
//                logger.info("Perfect solution found at generation {}", gen + 1);
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
//        int fitness = calculateFitness(bestIndividual);
//        int expectedScore = numClasses * SCHEDULES_PER_CLASS * 10;
//        logger.info("Best fitness score: {} (expected: {})", fitness, expectedScore);
//        if (fitness < expectedScore) {
//            logger.warn("Best solution has conflicts; attempting to repair...");
//            bestIndividual = repairConflicts(bestIndividual, classes);
//        }
//
//        Map<Long, List<Schedule>> classSchedules = bestIndividual.stream()
//                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));
//        List<List<Schedule>> result = new ArrayList<>();
//        for (SchoolClass schoolClass : classes) {
//            result.add(new ArrayList<>());
//        }
//
//        for (SchoolClass schoolClass : classes) {
//            Long classId = schoolClass.getId();
//            List<Schedule> classSchedule = classSchedules.getOrDefault(classId, Collections.emptyList());
//            Map<String, Integer> subjectFreq = new HashMap<>();
//            Map<String, Map<String, Integer>> subjectDayFreq = new HashMap<>();
//            Map<String, Set<String>> timeSlotConflicts = new HashMap<>();
//
//            List<Schedule> adjustedSchedules = new ArrayList<>();
//            for (Schedule s : classSchedule) {
//                String timeKey = s.getDay() + "-" + s.getTimeSlot();
//                String subjectName = s.getSubject().getName();
//
//                timeSlotConflicts.computeIfAbsent(timeKey, k -> new HashSet<>())
//                        .add(subjectName);
//
//                subjectFreq.merge(subjectName, 1, Integer::sum);
//                subjectDayFreq.computeIfAbsent(subjectName, k -> new HashMap<>())
//                        .merge(s.getDay(), 1, Integer::sum);
//
//                boolean valid = true;
//                if (timeSlotConflicts.get(timeKey).size() > 1) {
//                    valid = false;
//                }
//
//                if ("Math".equals(subjectName) || "Physics".equals(subjectName)) {
//                    if (subjectFreq.get(subjectName) > 2) {
//                        valid = false;
//                    }
//                    Map<String, Integer> dayFreq = subjectDayFreq.get(subjectName);
//                    for (int count : dayFreq.values()) {
//                        if (count > 1) {
//                            valid = false;
//                            break;
//                        }
//                    }
//                } else {
//                    if (subjectFreq.get(subjectName) > 1) {
//                        valid = false;
//                    }
//                }
//                if (valid) {
//                    adjustedSchedules.add(s);
//                }
//            }
//
//            while (adjustedSchedules.size() < SCHEDULES_PER_CLASS) {
//                Schedule newSchedule = createSingleScheduleWithConstraints(
//                        schoolClass, result, adjustedSchedules, subjectFreq, subjectDayFreq, timeSlotConflicts);
//                if (newSchedule != null) {
//                    adjustedSchedules.add(newSchedule);
//                    subjectFreq.merge(newSchedule.getSubject().getName(), 1, Integer::sum);
//                    subjectDayFreq.computeIfAbsent(newSchedule.getSubject().getName(), k -> new HashMap<>())
//                            .merge(newSchedule.getDay(), 1, Integer::sum);
//                    timeSlotConflicts.computeIfAbsent(newSchedule.getDay() + "-" + newSchedule.getTimeSlot(), k -> new HashSet<>())
//                            .add(newSchedule.getSubject().getName());
//                } else {
//                    logger.warn("Could not generate additional schedule for class {}. Attempting fallback.", schoolClass.getName());
//                    Schedule fallbackSchedule = createFallbackSchedule(schoolClass, result, timeSlotConflicts);
//                    if (fallbackSchedule != null) {
//                        adjustedSchedules.add(fallbackSchedule);
//                        logger.info("Added fallback schedule for class {}: {} on {} {}", schoolClass.getName(),
//                                fallbackSchedule.getSubject().getName(), fallbackSchedule.getDay(), fallbackSchedule.getTimeSlot());
//                    } else {
//                        logger.error("Failed to generate fallback schedule for class {}", schoolClass.getName());
//                        break;
//                    }
//                }
//            }
//
//            int index = classes.indexOf(schoolClass);
//            if (index >= 0 && index < result.size()) {
//                result.set(index, adjustedSchedules);
//            } else {
//                logger.error("Invalid index for class {}: {}", schoolClass.getName(), index);
//            }
//        }
//
//        return result;
//    }
//
//    /**
//     * Fallback method to create a simple schedule without strict constraints
//     */
//    private Schedule createFallbackSchedule(SchoolClass schoolClass, List<List<Schedule>> existingSchedules,
//                                            Map<String, Set<String>> timeSlotConflicts) {
//        List<Teacher> teachers = teacherRepository.findAll();
//        List<Classroom> classrooms = classroomRepository.findAll();
//        List<Subject> subjects = subjectRepository.findAll();
//        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
//        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");
//
//        Random random = new Random();
//        int attempts = 0;
//        int maxAttempts = 20;
//
//        while (attempts < maxAttempts) {
//            Subject subject = subjects.get(random.nextInt(subjects.size()));
//            List<Teacher> eligibleTeachers = teachers.stream()
//                    .filter(t -> t.getSubjects().contains(subject))
//                    .collect(Collectors.toList());
//            if (eligibleTeachers.isEmpty()) {
//                attempts++;
//                continue;
//            }
//
//            Teacher teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
//            Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
//            String day = days.get(random.nextInt(days.size()));
//            String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
//            String timeKey = day + "-" + timeSlot;
//
//            boolean conflict = existingSchedules.stream()
//                    .flatMap(List::stream)
//                    .anyMatch(s -> s.getDay().equals(day) && s.getTimeSlot().equals(timeSlot) &&
//                            (s.getTeacher().getId().equals(teacher.getId()) || s.getClassroom().getId().equals(classroom.getId())));
//
//            if (conflict || timeSlotConflicts.getOrDefault(timeKey, new HashSet<>()).size() > 0) {
//                attempts++;
//                continue;
//            }
//
//            return new Schedule(teacher, classroom, subject, schoolClass, day, timeSlot);
//        }
//        return null;
//    }
//
//    /**
//     * Repair conflicts by reassigning teachers and classrooms
//     */
//    private List<Schedule> repairConflicts(List<Schedule> individual, List<SchoolClass> classes) {
//        List<Teacher> allTeachers = teacherRepository.findAll();
//        List<Classroom> allClassrooms = classroomRepository.findAll();
//        Random random = new Random();
//
//        Map<String, Set<Long>> teacherSlots = new HashMap<>();
//        Map<String, Set<Long>> classroomSlots = new HashMap<>();
//        Map<Long, Map<String, Set<String>>> classTimeSlots = new HashMap<>();
//        Map<Long, Map<String, Integer>> subjectFrequency = new HashMap<>();
//        Map<Long, Map<String, Map<String, Integer>>> subjectDays = new HashMap<>();
//        List<Schedule> repaired = new ArrayList<>();
//
//        for (Schedule s : individual) {
//            String timeKey = s.getDay() + "-" + s.getTimeSlot();
//            Long classId = s.getSchoolClass().getId();
//            String subjectName = s.getSubject().getName();
//
//            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
//
//            while (!teacherSlots.get(timeKey).add(s.getTeacher().getId())) {
//                List<Teacher> availableTeachers = allTeachers.stream()
//                        .filter(t -> t.getSubjects().contains(s.getSubject()))
//                        .filter(t -> !teacherSlots.get(timeKey).contains(t.getId()))
//                        .collect(Collectors.toList());
//                if (!availableTeachers.isEmpty()) {
//                    s.setTeacher(availableTeachers.get(random.nextInt(availableTeachers.size())));
//                } else {
//                    break;
//                }
//            }
//
//            while (!classroomSlots.get(timeKey).add(s.getClassroom().getId())) {
//                List<Classroom> availableClassrooms = allClassrooms.stream()
//                        .filter(c -> !classroomSlots.get(timeKey).contains(c.getId()))
//                        .collect(Collectors.toList());
//                if (!availableClassrooms.isEmpty()) {
//                    s.setClassroom(availableClassrooms.get(random.nextInt(availableClassrooms.size())));
//                } else {
//                    break;
//                }
//            }
//
//            classTimeSlots.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(timeKey, k -> new HashSet<>())
//                    .add(subjectName);
//
//            subjectFrequency.computeIfAbsent(classId, k -> new HashMap<>())
//                    .merge(subjectName, 1, Integer::sum);
//            subjectDays.computeIfAbsent(classId, k -> new HashMap<>())
//                    .computeIfAbsent(subjectName, k -> new HashMap<>())
//                    .merge(s.getDay(), 1, Integer::sum);
//
//            boolean valid = true;
//            if (classTimeSlots.get(classId).get(timeKey).size() > 1) {
//                valid = false;
//            }
//
//            if ("Math".equals(subjectName) || "Physics".equals(subjectName)) {
//                int freq = subjectFrequency.get(classId).get(subjectName);
//                Map<String, Integer> dayFreq = subjectDays.get(classId).get(subjectName);
//                if (freq > 2) {
//                    valid = false;
//                }
//                for (int count : dayFreq.values()) {
//                    if (count > 1) {
//                        valid = false;
//                        break;
//                    }
//                }
//            } else {
//                if (subjectFrequency.get(classId).get(subjectName) > 1) {
//                    valid = false;
//                }
//            }
//
//            if (valid) {
//                repaired.add(s);
//            }
//        }
//        return repaired;
//    }
//
//    /**
//     * Create a single schedule with constraints, ensuring teacher consistency
//     */
//    private Schedule createSingleScheduleWithConstraints(SchoolClass schoolClass, List<List<Schedule>> existingSchedules,
//                                                         List<Schedule> currentClassSchedules, Map<String, Integer> subjectFreq,
//                                                         Map<String, Map<String, Integer>> subjectDayFreq,
//                                                         Map<String, Set<String>> timeSlotConflicts) {
//        List<Teacher> teachers = teacherRepository.findAll();
//        List<Classroom> classrooms = classroomRepository.findAll();
//        List<Subject> subjects = subjectRepository.findAll();
//        List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
//        List<String> timeSlots = Arrays.asList("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00", "16:00 - 18:00");
//
//        Random random = new Random();
//        int attempts = 0;
//        int maxAttempts = 30;
//
//        while (attempts < maxAttempts) {
//            Subject subject = subjects.get(random.nextInt(subjects.size()));
//            String subjectName = subject.getName();
//            int currentFreq = subjectFreq.getOrDefault(subjectName, 0);
//            Map<String, Integer> scheduledDays = subjectDayFreq.getOrDefault(subjectName, new HashMap<>());
//
//            boolean isMathOrPhysics = "Math".equals(subjectName) || "Physics".equals(subjectName);
//            if (isMathOrPhysics) {
//                if (currentFreq >= 2) {
//                    attempts++;
//                    continue;
//                }
//            } else {
//                if (currentFreq >= 1) {
//                    attempts++;
//                    continue;
//                }
//            }
//
//            String day = days.get(random.nextInt(days.size()));
//            if (isMathOrPhysics && scheduledDays.getOrDefault(day, 0) >= 1) {
//                attempts++;
//                continue;
//            }
//
//            String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
//            String timeKey = day + "-" + timeSlot;
//
//            if (timeSlotConflicts.getOrDefault(timeKey, new HashSet<>()).size() > 0) {
//                attempts++;
//                continue;
//            }
//
//            Teacher teacher;
//            List<Schedule> existingSubjectSchedules = currentClassSchedules.stream()
//                    .filter(s -> s.getSubject().equals(subject))
//                    .collect(Collectors.toList());
//            if (!existingSubjectSchedules.isEmpty()) {
//                teacher = existingSubjectSchedules.get(0).getTeacher(); // Use same teacher
//            } else {
//                List<Teacher> eligibleTeachers = teachers.stream()
//                        .filter(t -> t.getSubjects().contains(subject))
//                        .collect(Collectors.toList());
//                if (eligibleTeachers.isEmpty()) {
//                    attempts++;
//                    continue;
//                }
//                teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
//            }
//
//            Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
//
//            boolean conflict = existingSchedules.stream()
//                    .flatMap(List::stream)
//                    .anyMatch(s -> s.getDay().equals(day) && s.getTimeSlot().equals(timeSlot) &&
//                            (s.getTeacher().getId().equals(teacher.getId()) || s.getClassroom().getId().equals(classroom.getId())));
//            if (conflict) {
//                attempts++;
//                continue;
//            }
//
//            return new Schedule(teacher, classroom, subject, schoolClass, day, timeSlot);
//        }
//        logger.warn("Failed to find a non-conflicting slot for class {} after {} attempts.", schoolClass.getName(), maxAttempts);
//        return null;
//    }
//}
//
//
//
//
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

    private static final int POPULATION_SIZE = 2000;
    private static final double MUTATION_RATE = 0.2;
    private static final int SCHEDULES_PER_CLASS = 10;
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
        List<Subject> subjectsToSchedule = new ArrayList<>();
        for (Subject subject : subjects) {
            int count = ("Math".equals(subject.getName()) || "Physics".equals(subject.getName())) ? 2 : 1;
            for (int i = 0; i < count; i++) {
                subjectsToSchedule.add(subject);
            }
        }
        Collections.shuffle(subjectsToSchedule);


        Map<String, Teacher> subjectTeacherMap = new HashMap<>();
        Map<String, Set<String>> scheduledTimes = new HashMap<>();

        for (Subject subject : subjectsToSchedule) {
            String subjectName = subject.getName();
            boolean isMathOrPhysics = "Math".equals(subjectName) || "Physics".equals(subjectName);


            Teacher teacher = subjectTeacherMap.get(subjectName);
            if (teacher == null) {
                List<Teacher> eligibleTeachers = teachers.stream()
                        .filter(t -> t.getSubjects().contains(subject))
                        .collect(Collectors.toList());
                if (eligibleTeachers.isEmpty()) continue;
                teacher = eligibleTeachers.get(random.nextInt(eligibleTeachers.size()));
                subjectTeacherMap.put(subjectName, teacher); 
            }

            for (int attempt = 0; attempt < 100; attempt++) {
                String day = days.get(random.nextInt(days.size()));
                String timeSlot = timeSlots.get(random.nextInt(timeSlots.size()));
                String timeKey = day + "-" + timeSlot;

                if (scheduledTimes.containsKey(timeKey)) continue;
                if (isMathOrPhysics && schedule.stream()
                        .filter(s -> s.getSubject().equals(subject))
                        .anyMatch(s -> s.getDay().equals(day))) continue;

                Classroom classroom = findAvailableClassroom(timeKey, classrooms, schedule);
                if (classroom == null) continue;

                Schedule newSchedule = new Schedule(teacher, classroom, subject, schoolClass, day, timeSlot);
                schedule.add(newSchedule);
                scheduledTimes.put(timeKey, new HashSet<>());
                break;
            }
        }
        return schedule;
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

        for (Schedule s : individual) {
            String timeKey = s.getDay() + "-" + s.getTimeSlot();
            Long classId = s.getSchoolClass().getId();
            String subjectName = s.getSubject().getName();

            teacherSlots.computeIfAbsent(timeKey, k -> new HashSet<>());
            classroomSlots.computeIfAbsent(timeKey, k -> new HashSet<>());

            if (!teacherSlots.get(timeKey).add(s.getTeacher().getId())) {
                score -= 1000;
            } else {
                score += 10;
            }

            if (!classroomSlots.get(timeKey).add(s.getClassroom().getId())) {
                score -= 1000;
            } else {
                score += 10;
            }

            classTimeSlots.computeIfAbsent(classId, k -> new HashMap<>())
                    .computeIfAbsent(timeKey, k -> new HashSet<>())
                    .add(subjectName);

            if (classTimeSlots.get(classId).get(timeKey).size() > 1) {
                score -= 5000;
            }

            classSubjectFrequency.computeIfAbsent(classId, k -> new HashMap<>())
                    .merge(subjectName, 1, Integer::sum);
            classSubjectDayFrequency.computeIfAbsent(classId, k -> new HashMap<>())
                    .computeIfAbsent(subjectName, k -> new HashMap<>())
                    .merge(s.getDay(), 1, Integer::sum);

            if ("Math".equals(subjectName) || "Physics".equals(subjectName)) {
                int frequency = classSubjectFrequency.get(classId).get(subjectName);
                Map<String, Integer> dayFreq = classSubjectDayFrequency.get(classId).get(subjectName);
                if (frequency > 2) score -= 5000;
                if (dayFreq.values().stream().anyMatch(c -> c > 1)) score -= 5000;
            } else if (classSubjectFrequency.get(classId).get(subjectName) > 1) {
                score -= 5000;
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
            if (("Math".equals(subjectName) || "Physics".equals(subjectName)) && currentCount >= 2) continue;
            if (currentCount >= 1 && !("Math".equals(subjectName) || "Physics".equals(subjectName))) continue;

            teacherSlots.get(timeKey).add(s.getTeacher().getId());
            classroomSlots.get(timeKey).add(s.getClassroom().getId());
            subjectCounts.get(classId).merge(subjectName, 1, Integer::sum);
            repaired.add(s);
        }
        return repaired;
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
        classes = classes.subList(0, numClasses);

        List<List<Schedule>> population = initializePopulation(classes);

        for (int gen = 0; gen < Math.max(generations, MAX_GENERATIONS); gen++) {
            List<List<Schedule>> newPopulation = new ArrayList<>();
            int elitismCount = (int)(POPULATION_SIZE * 0.1);

            // Elitism: Keep top 10%
            population.stream()
                    .sorted(Comparator.comparingInt(this::calculateFitness).reversed())
                    .limit(elitismCount)
                    .forEach(newPopulation::add);

            // Fill rest with offspring
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
            int maxScore = numClasses * SCHEDULES_PER_CLASS * 10;
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

        // Final validation and repair
        bestIndividual = repairConflicts(bestIndividual, classes);
        Map<Long, List<Schedule>> classSchedules = bestIndividual.stream()
                .collect(Collectors.groupingBy(s -> s.getSchoolClass().getId()));
        List<List<Schedule>> result = new ArrayList<>(Collections.nCopies(classes.size(), null));

        for (SchoolClass schoolClass : classes) {
            int index = classes.indexOf(schoolClass);
            result.set(index, classSchedules.getOrDefault(schoolClass.getId(), Collections.emptyList()));
        }

        logger.info("Best fitness score: {}", calculateFitness(bestIndividual));
        return result;
    }
}



