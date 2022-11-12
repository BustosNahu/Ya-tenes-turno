package com.yatenesturno;

public interface Constants {

    /* USER MANAGEMENT */
    int RC_SIGN_IN_ACTIVITY = 2;
    int RC_NEW_PLACE = 3;
    int RC_EDIT_JOB = 4;
    int RC_EDIT_SERVICES = 5;
    int RC_JOB_REQUESTS = 6;
    int RC_EDIT_PLACE = 7;
    int RC_EDIT_HOURS = 8;
    int RC_GET_PREMIUM = 9;
    int RC_NEW_ANONYMOUS_APP = 10;
    int RC_SETTINGS = 11;
    int RC_UPDATE = 12;

    int DATABASE_TIMEOUT = 10000;
    int CONNECTION_TIMEOUT = 30 * 1000;

    /* Directories */
    String PROFILE_IMAGE_DIRECTORY = "profile_pic_images";
    String PLACE_IMAGE_DIRECTORY = "job_images";

    /* Shared Preferences */
    String SHARED_PREF_NAME = "SharedPref";
    String SHARED_PREF_ACCESS_TOKEN = "AccessToken";
    String SHARED_PREF_USER = "user";
    String SHARED_PREF_AUTHENTICATOR = "AuthenticatorInUse";
    String SHARED_PREF_SCHEDULE_EDIT_WARNING = "scheduleEditWarning";
    String SHARED_PREF_SHOW_MESSAGE_AGAIN = "shouldShowMessage";

    /* rest urls */
    String DJANGO_URL_CONVERT_TOKEN = "/auth/login-android-google/";
    String DJANGO_URL_APPOINTMENTS = "/android/appointments/";
    String DJANGO_URL_GET_PREMIUM_PLACES = "/android/profile/get-premium-places/";
    String DJANGO_URL_GET_OWNED_PLACES = "/android/profile/places/";
    String DJANGO_URL_NEW_PLACE = "/android/new-place/";
    String DJANGO_URL_JOBTYPES = "/android/jobtypes/";
    String DJANGO_URL_PLACE_DOES = "/android/profile/place-does/";
    String DJANGO_URL_JOB_REQUEST = "/android/profile/job-requests/";
    String DJANGO_URL_ACCEPT_JOB_REQUEST = "/android/profile/accept-job-request/";
    String DJANGO_URL_CANCEL_JOB_REQUEST = "/android/profile/cancel-job-request/";
    String DJANGO_URL_SEARCH_PLACE = "/android/search-place/";
    String DJANGO_URL_NEW_JOB_REQUEST = "/android/new-job-request/";
    String DJANGO_URL_NEW_DAY_SCHEDULE = "/android/profile/new-day-schedule/";
    String DJANGO_URL_DOABLE_SERVICES = "/android/doable-services/";
    String DJANGO_URL_NEW_SERVICE_INSTANCE = "/android/profile/new-service-instance/";
    String DJANGO_URL_DROP_PLACE = "/android/drop-place/";
    String DJANGO_URL_DROP_JOB = "/android/drop-job/";
    String DJANGO_URL_NEW_OWNER_JOB = "/android/new-owner-job/";
    String DJANGO_URL_JOBS_IN_PLACE = "/android/jobs-in-place/";
    String DJANGO_URL_CLIENTS_OF_JOB = "/android/clients-of-job/";
    String DJANGO_URL_NOTIFICATION_METHOD = "/android/notification-method/";
    String DJANGO_URL_CHANGE_JOB_PERMISSIONS = "/android/update-job-permissions/";
    String DJANGO_URL_UPDATE_PLACE = "/android/update-place/";
    String DJANGO_URL_DROP_APPOINTMENT = "/android/drop-appointment/";
    String DJANGO_URL_UPCOMING_APPOINTMENTS = "/android/upcoming-appointments/";
    String DJANGO_URL_SIGN_OUT = "/auth/logout-android/";
    String DJANGO_URL_UPDATE_FCM_TOKEN = "/auth/update-fcm-token/";
    String DJANGO_URL_GET_AVAILABLE_APPOINTMENTS = "/api/place/%s/jobs/%s/available-appointments/%s";
    String DJANGO_URL_NEW_ANONYMOUS_APP = "/android/new-anonymous-app/";
    String DJANGO_URL_DROP_CLASS_APPOINTMENT = "/android/drop-class-appointments/";
    String DJANGO_URL_DROP_CLIENT_CLASS_APPOINTMENT = "/android/drop-client-class-appointments/";
    String DJANGO_URL_FETCH_MESSAGE = "/android/fetch-messages/";
    String DJANGO_URL_POST_STATS = "/stats/post-stats/";
    String DJANGO_URL_ACCEPT_FETCH_SUBSCRIPTIONS = "/android/fetch-subscriptions/";
    String DJANGO_URL_ACCEPT_VALIDATE_SUB_TOKEN = "/android/validate-sub-token/";
    String DJANGO_URL_ON_SUBSCRIPTION_APPROVED = "/android/subscription-approved/";
    String DJANGO_URL_GET_SUBSCRIPTIONS = "/android/get-subscriptions/";
    String DJANGO_URL_UNSUBSCRIBE = "/android/unsubscribe/";
    String DJANGO_URL_JOB_CONFIG = "/android/user_cancellable_apps/";
    String DJANGO_URL_GET_DAY_SCHEDULES = "/android/get-day-schedules/";
    String DJANGO_URL_SERVICE_INSTANCES = "/android/get-service-instances/";
    String DJANGO_URL_SET_LABEL = "/android/place/%s/job/%s/appointments/%s/label";
    String DJANGO_URL_SET_ATTENDED = "/android/place/%s/job/%s/appointments/%s/attended";
    String DJANGO_URL_LABELS = "/android/place/%s/job/%s/labels";
    String DJANGO_URL_LABELS_DELETE = "/android/place/%s/job/%s/labels/%s";
    String DJANGO_URL_EMERGENCY = "/android/place/%s/job/%s/emergency";
    String DJANGO_URL_EMERGENCY_LOCATIONS = "/android/job/%s/emergency-locations";
    String DJANGO_URL_EMERGENCY_LOCATIONS_UPDATE = "/android/job/%s/emergency-locations/%s";
    String DJANGO_URL_CREDITS = "/android/place/%s/job/%s/credits";
    String DJANGO_URL_CREDIT_PACKS = "/android/credit-packs";
    String DJANGO_URL_DAYS_OFF = "/android/job/%s/days-off";
    String DJANGO_URL_DAYS_OFF_UPDATE = "/android/job/%s/days-off/%s";
    String DJANGO_URL_APPOINTMENT_OBSERVATION = "/android/job/%s/appointments/%s/observation";
    String DJANGO_URL_PLACE_CREDITS = "/place-credits/places/%s/clients/%s/credits";
    String DJANGO_URL_PLACE_CREDITS_UPDATE = "/place-credits/places/%s/clients/%s/credits/%s";
    String DJANGO_URL_REMOVE_CLIENT = "/android/job/%s/client/%s";
    String DJANGO_URL_CLIENTS_OF_PLACE = "/android/place/%s/clients";
    String DJANGO_URL_FETCH_SI_WITH_CREDITS = "/place-credits/places/%s/service-instances";
}
