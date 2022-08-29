package app.rubbickcube.seatcheck.di.component;


import javax.inject.Singleton;

import app.rubbickcube.seatcheck.MainActivity;
import app.rubbickcube.seatcheck.SeatCheckUserActivity;
import app.rubbickcube.seatcheck.SettingsActivity;
import app.rubbickcube.seatcheck.activities.ActivityEndMeeting;
import app.rubbickcube.seatcheck.activities.ActivitySeatAccepted;
import app.rubbickcube.seatcheck.activities.ActivitySeatLive;
import app.rubbickcube.seatcheck.activities.ActivitySeatLiveInMeeting;
import app.rubbickcube.seatcheck.activities.ActivityShowInvites;
import app.rubbickcube.seatcheck.activities.InviteSentActivity;
import app.rubbickcube.seatcheck.activities.NeedASeatActivity;
import app.rubbickcube.seatcheck.activities.PostSeatActivity;
import app.rubbickcube.seatcheck.activities.ProfileActivity;
import app.rubbickcube.seatcheck.adapters.PostOnListAdapter;
import app.rubbickcube.seatcheck.di.module.BackendLessModule;
import app.rubbickcube.seatcheck.fragments.AboutFragment;
import app.rubbickcube.seatcheck.fragments.ReviewFragments;
import dagger.Component;

@Singleton
@Component(modules = BackendLessModule.class)
public interface AppComponent {

    void inject(NeedASeatActivity needASeatActivity);
    void inject(AboutFragment aboutFragment);
    void inject(PostSeatActivity postSeatActivity);
    void inject(ProfileActivity profileActivity);
    void inject(MainActivity profileActivity);
    void inject(SeatCheckUserActivity seatCheckUserActivity);
    void inject(ActivityEndMeeting activityEndMeeting);
    void inject(ActivitySeatLiveInMeeting activitySeatLiveInMeeting);
    void inject(PostOnListAdapter postOnListAdapter);
    void inject(SettingsActivity settingsActivity);
    void inject(ReviewFragments reviewFragments);
    void inject(ActivitySeatAccepted activitySeatAccepted);
    void inject(InviteSentActivity inviteSentActivity);
    void inject(ActivityShowInvites activityShowInvites);
    void inject(ActivitySeatLive activitySeatLive);
}
