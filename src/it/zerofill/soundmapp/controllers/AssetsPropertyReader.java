package it.zerofill.soundmapp.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;

public class AssetsPropertyReader {
	private Context context;
    private Properties properties;

    public AssetsPropertyReader(Context context) {
           this.context = context;
           properties = new Properties();
    }

    public Properties getProperties(String FileName) {
           try {
                  /**
                   * getAssets() Return an AssetManager instance for your
                   * application's package. AssetManager Provides access to an
                   * application's raw asset files;
                   */
                  AssetManager assetManager = context.getAssets();
                  /**
                   * Open an asset using ACCESS_STREAMING mode. This
                   */
                  InputStream inputStream = assetManager.open(FileName);
                  /**
                   * Loads properties from the specified InputStream,
                   */
                  properties.load(inputStream);

           } catch (IOException e) {
           }
           return properties;
    }
}
