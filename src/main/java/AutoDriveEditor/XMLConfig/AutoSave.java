package AutoDriveEditor.XMLConfig;

import AutoDriveEditor.Classes.NameableThread;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogAutoSaveMenu.bDebugLogAutoSave;
import static AutoDriveEditor.RoadNetwork.RoadMap.getRoadMap;
import static AutoDriveEditor.XMLConfig.EditorXML.autoSaveInterval;
import static AutoDriveEditor.XMLConfig.EditorXML.maxAutoSaveSlots;
import static AutoDriveEditor.XMLConfig.GameXML.autoSaveGameConfigFile;
import static AutoDriveEditor.XMLConfig.RoutesXML.autoSaveRouteManagerXML;

public class AutoSave {


    public static ScheduledExecutorService scheduledExecutorService;
    @SuppressWarnings("rawtypes")
    public static ScheduledFuture scheduledFuture;

    private static boolean bAutoSaveSuspend;

    public static void startAutoSaveThread() {
        LOG.info("Starting AutoSave Thread");

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NameableThread(Executors.defaultThreadFactory(), "AutoSave"));
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (getRoadMap() != null) {
                if (configType == CONFIG_SAVEGAME) {
                    autoSaveGameConfigFile();
                } else if (configType == CONFIG_ROUTEMANAGER) {
                    autoSaveRouteManagerXML();
                }
            }
        }, autoSaveInterval, autoSaveInterval, TimeUnit.MINUTES);
//
        LOG.info("Started AutoSave Thread ( Interval: {} Minutes , Max Slots {} )", autoSaveInterval, maxAutoSaveSlots);
    }

    public static void stopAutoSaveThread() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            try {
                if (!scheduledExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduledExecutorService.shutdownNow();
                    if (!scheduledExecutorService.awaitTermination(3, TimeUnit.SECONDS)) {
                        LOG.info("AutoSave thread failed to shut down after 5 seconds");
                    }
                } else {
                    LOG.info("AutoSave thread stopped");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                scheduledExecutorService.shutdownNow();
            }
        }
    }

    public static void restartAutoSaveThread() {
        LOG.info("Restarting AutoSave Thread");
        if (scheduledExecutorService != null) {
            stopAutoSaveThread();
            startAutoSaveThread();
        } else {
            LOG.info("Failed to restart AutoSave Thread ( Thread not active )");
        }
    }

    public static void suspendAutoSaving() {
        if (bDebugLogAutoSave && bAutoSaveSuspend) {
            LOG.warn("## WARNING ## Suspend AutoSave received while already suspended");
            new Exception().printStackTrace();
        }
        bAutoSaveSuspend = true; }

    public static void resumeAutoSaving() {
        if (bDebugLogAutoSave && !bAutoSaveSuspend) {
            LOG.warn("## WARNING ## Resuming AutoSave received while already enabled");
            new Exception().printStackTrace();
        }
        bAutoSaveSuspend = false; }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canAutoSave() { return !bAutoSaveSuspend; }

}