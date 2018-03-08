package com.baddog.optionsscanner;

import android.app.Application;
import android.app.Application;
import android.content.Intent;

import com.facebook.stetho.Stetho;


import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class OptionScannerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .inMemory()
                .name("myrealm.realm").build();
        Realm.setDefaultConfiguration(config);

        // Create an InitializerBuilder
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector( Stetho.defaultInspectorModulesProvider(this) );
        // Enable command line interface
        initializerBuilder.enableDumpapp( Stetho.defaultDumperPluginsProvider(this) );
        // Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();
        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer);



        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

