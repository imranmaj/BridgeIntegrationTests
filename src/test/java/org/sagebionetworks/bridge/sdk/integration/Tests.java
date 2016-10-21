package org.sagebionetworks.bridge.sdk.integration;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.LocalTime;
import org.joda.time.Period;

import org.sagebionetworks.bridge.sdk.ClientInfo;
import org.sagebionetworks.bridge.sdk.ClientProvider;
import org.sagebionetworks.bridge.sdk.Config;
import org.sagebionetworks.bridge.sdk.models.schedules.ABTestScheduleStrategy;
import org.sagebionetworks.bridge.sdk.models.schedules.Activity;
import org.sagebionetworks.bridge.sdk.models.schedules.Schedule;
import org.sagebionetworks.bridge.sdk.models.schedules.SchedulePlan;
import org.sagebionetworks.bridge.sdk.models.schedules.ScheduleType;
import org.sagebionetworks.bridge.sdk.models.schedules.SimpleScheduleStrategy;
import org.sagebionetworks.bridge.sdk.models.schedules.TaskReference;
import org.sagebionetworks.bridge.sdk.models.studies.OperatingSystem;
import org.sagebionetworks.bridge.sdk.models.studies.Study;
import org.sagebionetworks.bridge.sdk.rest.model.EmailTemplate;
import org.sagebionetworks.bridge.sdk.rest.model.MimeType;

import com.google.common.collect.Sets;

public class Tests {
    
    public static final String APP_NAME = "Integration Tests";
    
    public static final ClientInfo TEST_CLIENT_INFO = new ClientInfo.Builder().withAppName(APP_NAME).withAppVersion(0).build();
    
    public static final String TEST_KEY = "api";
    
    public static final EmailTemplate TEST_RESET_PASSWORD_TEMPLATE = new EmailTemplate().subject("Reset your password")
        .body("<p>${url}</p>").mimeType(MimeType.TEXT_HTML);
    public static final EmailTemplate TEST_VERIFY_EMAIL_TEMPLATE = new EmailTemplate().subject("Verify your email")
        .body("<p>${url}</p>").mimeType(MimeType.TEXT_HTML);

    public static final org.sagebionetworks.bridge.sdk.models.studies.EmailTemplate TEST_RESET_PASSWORD_TEMPLATE_OLD = new org.sagebionetworks.bridge.sdk.models.studies.EmailTemplate(
            "Reset your password", "<p>${url}</p>",
            org.sagebionetworks.bridge.sdk.models.studies.EmailTemplate.MimeType.HTML);
    public static final org.sagebionetworks.bridge.sdk.models.studies.EmailTemplate TEST_VERIFY_EMAIL_TEMPLATE_OLD = new org.sagebionetworks.bridge.sdk.models.studies.EmailTemplate(
            "Verify your email", "<p>${url}</p>",
            org.sagebionetworks.bridge.sdk.models.studies.EmailTemplate.MimeType.HTML);
        
    public static String randomIdentifier(Class<?> cls) {
        return ("sdk-" + cls.getSimpleName().toLowerCase() + "-" + RandomStringUtils.randomAlphabetic(5)).toLowerCase();
    }

    public static String makeEmail(Class<?> cls) {
        Config config = ClientProvider.getConfig();
        String devName = config.getDevName();
        String clsPart = cls.getSimpleName();
        String rndPart = RandomStringUtils.randomAlphabetic(4);
        return String.format("bridge-testing+%s-%s-%s@sagebase.org", devName, clsPart, rndPart);
    }
    
    // This seems like something that should be added to schedule.
    private static void setTaskActivity(Schedule schedule, String taskIdentifier) {
        checkNotNull(taskIdentifier);
        schedule.addActivity(new Activity("Task activity", null, new TaskReference(taskIdentifier)));
    }
    
    public static SchedulePlan getABTestSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("A/B Test Schedule Plan");
        Schedule schedule1 = new Schedule();
        schedule1.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        setTaskActivity(schedule1, "task:AAA");
        schedule1.setExpires(Period.parse("PT1H"));
        schedule1.setLabel("Test label for the user");
        
        Schedule schedule2 = new Schedule();
        schedule2.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        setTaskActivity(schedule2, "task:BBB");
        schedule2.setExpires(Period.parse("PT1H"));
        schedule2.setLabel("Test label for the user");

        Schedule schedule3 = new Schedule();
        schedule3.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        setTaskActivity(schedule3, "task:CCC");
        // This doesn't exist and now it matters, because we look for a survey to update the identifier
        // setSurveyActivity(schedule3, "identifier", "GUID-AAA", DateTime.parse("2015-01-27T17:46:31.237Z"));
        schedule3.setExpires(Period.parse("PT1H"));
        schedule3.setLabel("Test label for the user");

        ABTestScheduleStrategy strategy = new ABTestScheduleStrategy();
        strategy.addGroup(40, schedule1);
        strategy.addGroup(40, schedule2);
        strategy.addGroup(20, schedule3);
        plan.setStrategy(strategy);
        return plan;
    }
    
    public static SchedulePlan getSimpleSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("Cron-based schedule");
        Schedule schedule = new Schedule();
        schedule.setCronTrigger("0 0 11 ? * MON,WED,FRI *");
        setTaskActivity(schedule, "task:CCC");
        schedule.setExpires(Period.parse("PT1H"));
        schedule.setLabel("Test label for the user");

        plan.setSchedule(schedule);
        return plan;
    }
    
    public static SchedulePlan getDailyRepeatingSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("Daily repeating schedule plan");
        Schedule schedule = new Schedule();
        schedule.setScheduleType(ScheduleType.RECURRING);
        schedule.setInterval(Period.parse("P1D"));
        schedule.setExpires(Period.parse("P1D"));
        schedule.addTimes(LocalTime.parse("12:00"));
        setTaskActivity(schedule, "task:CCC");
        schedule.setLabel("Test label for the user");
        plan.setSchedule(schedule);
        return plan;
    }
    
    public static SchedulePlan getPersistentSchedulePlan() {
        SchedulePlan plan = new SchedulePlan();
        plan.setLabel("Persistent schedule");
        Schedule schedule = new Schedule();
        setTaskActivity(schedule, "CCC");
        schedule.setEventId("task:"+schedule.getActivities().get(0).getTask().getIdentifier()+":finished");
        schedule.setLabel("Test label");

        plan.setSchedule(schedule);
        return plan;
    }

    public static Schedule getSimpleSchedule(SchedulePlan plan) {
        return ((SimpleScheduleStrategy)plan.getStrategy()).getSchedule();
    }
    
    public static List<Activity> getActivitiesFromSimpleStrategy(SchedulePlan plan) {
        return ((SimpleScheduleStrategy)plan.getStrategy()).getSchedule().getActivities();    
    }
    
    public static Activity getActivityFromSimpleStrategy(SchedulePlan plan) {
        return ((SimpleScheduleStrategy)plan.getStrategy()).getSchedule().getActivities().get(0);    
    }
    
    public static Study getStudy(String identifier, Long version) {
        Study study = new Study();
        study.setIdentifier(identifier);
        study.setMinAgeOfConsent(18);
        study.setName("Test Study [SDK]");
        study.setSponsorName("The Test Study Folks [SDK]");
        study.setSupportEmail("test@test.com");
        study.setConsentNotificationEmail("test2@test.com");
        study.setTechnicalEmail("test3@test.com");
        study.setUsesCustomExportSchedule(true);
        study.getUserProfileAttributes().add("new_profile_attribute");
        study.setTaskIdentifiers(Sets.newHashSet("taskA")); // setting it differently just for the heck of it 
        study.setDataGroups(Sets.newHashSet("beta_users", "production_users"));
        study.setResetPasswordTemplate(Tests.TEST_RESET_PASSWORD_TEMPLATE_OLD);
        study.setVerifyEmailTemplate(Tests.TEST_VERIFY_EMAIL_TEMPLATE_OLD);
        study.setHealthCodeExportEnabled(true);
        study.getMinSupportedAppVersions().put(OperatingSystem.ANDROID, 10);
        study.getMinSupportedAppVersions().put(OperatingSystem.IOS, 14);
        if (version != null) {
            study.setVersion(version);
        }
        return study;
    }
    
    /**
     * Guava does not have a version of this method that also lets you add items.
     */
    @SuppressWarnings("unchecked")
    public static <T> LinkedHashSet<T> newLinkedHashSet(T... items) {
        LinkedHashSet<T> set = new LinkedHashSet<T>();
        for (T item : items) {
            set.add(item);    
        }
        return set;
    }
    
}
