package com.gettingmobile.goodnews;

import com.gettingmobile.android.util.ApiLevel;
import com.gettingmobile.goodnews.settings.FullSettingsActivity;
import com.gettingmobile.goodnews.settings.RootSettingsActivity;
import com.gettingmobile.goodnews.settings.SettingsActivity;
import com.gettingmobile.goodnews.settings.SettingsIntentFactory;
import com.gettingmobile.goodnews.sync.SyncServiceProxy;
import com.gettingmobile.goodnews.util.ThemeUtil;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class InjectionModule extends AbstractModule {
    public static final String IS_TABLET = "isTablet";
    private final Application app;

    public InjectionModule(Application app) {
        this.app = app;
    }
    
    @Override
    protected void configure() {
        bind(Application.class).toInstance(app);
        bind(SyncServiceProxy.class).toInstance(app.getSyncService());
        bind(new TypeLiteral<Class<? extends FullSettingsActivity>>() {}).toInstance(
                ApiLevel.isAtLeast(ApiLevel.V_3_0) ? SettingsActivity.class : RootSettingsActivity.class);
        bindConstant().annotatedWith(Names.named(IS_TABLET)).to(ThemeUtil.isTablet(app));
        bind(SettingsIntentFactory.class);
    }
}
