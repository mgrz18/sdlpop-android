package com.mgrz18.sdlpop;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class AssetExtractor {

    private static final String TAG = "SDLPoP-Assets";
    private static final String ROOT_ASSET_DIR = "data";
    private static final String MARKER_NAME = ".assets_extracted_v1";

    private AssetExtractor() {}

    static void extractIfNeeded(Context context) {
        File internalDir = context.getFilesDir();
        File marker = new File(internalDir, MARKER_NAME);
        if (marker.exists()) {
            Log.i(TAG, "Assets already extracted, skipping.");
            return;
        }
        Log.i(TAG, "Extracting assets to " + internalDir.getAbsolutePath());
        try {
            File rootOut = new File(internalDir, ROOT_ASSET_DIR);
            copyAssetDir(context.getAssets(), ROOT_ASSET_DIR, rootOut, rootOut.getCanonicalFile());
            if (!marker.createNewFile()) {
                Log.w(TAG, "Could not create marker file.");
            }
            Log.i(TAG, "Asset extraction complete.");
        } catch (IOException e) {
            Log.e(TAG, "Asset extraction failed", e);
        }
    }

    private static void copyAssetDir(AssetManager assets, String assetPath, File outDir, File rootCanonical) throws IOException {
        ensureWithinRoot(rootCanonical, outDir);
        String[] entries = assets.list(assetPath);
        if (entries == null || entries.length == 0) {
            copyAssetFile(assets, assetPath, outDir, rootCanonical);
            return;
        }
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + outDir);
        }
        for (String entry : entries) {
            String childAssetPath = assetPath + "/" + entry;
            File childOut = new File(outDir, entry);
            String[] grandChildren = assets.list(childAssetPath);
            if (grandChildren != null && grandChildren.length > 0) {
                copyAssetDir(assets, childAssetPath, childOut, rootCanonical);
            } else {
                copyAssetFile(assets, childAssetPath, childOut, rootCanonical);
            }
        }
    }

    private static void copyAssetFile(AssetManager assets, String assetPath, File outFile, File rootCanonical) throws IOException {
        ensureWithinRoot(rootCanonical, outFile);
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create parent: " + parent);
        }
        try (InputStream in = assets.open(assetPath);
             OutputStream out = new FileOutputStream(outFile)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                if (n > 0) out.write(buf, 0, n);
            }
        }
    }

    private static void ensureWithinRoot(File rootCanonical, File child) throws IOException {
        String rootPath = rootCanonical.getPath();
        String childPath = child.getCanonicalPath();
        if (!childPath.equals(rootPath) && !childPath.startsWith(rootPath + File.separator)) {
            throw new IOException("Refusing path traversal: " + childPath + " escapes " + rootPath);
        }
    }
}
