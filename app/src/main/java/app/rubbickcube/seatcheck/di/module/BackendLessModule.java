package app.rubbickcube.seatcheck.di.module;



import com.backendless.Backendless;
import com.backendless.BackendlessUser;

import javax.inject.Singleton;


import dagger.Module;
import dagger.Provides;

@Module
public class BackendLessModule {

    @Provides @Singleton
    BackendlessUser providesBackendLessUser() {
        return Backendless.UserService.CurrentUser();
    }
}
