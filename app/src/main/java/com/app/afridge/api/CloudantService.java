package com.app.afridge.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.activeandroid.query.Select;
import com.app.afridge.AuthState;
import com.app.afridge.dom.FridgeItem;
import com.app.afridge.dom.HistoryItem;
import com.app.afridge.dom.ItemList;
import com.app.afridge.dom.NoteItem;
import com.app.afridge.dom.SyncEvent;
import com.app.afridge.dom.User;
import com.app.afridge.dom.enums.ChangeType;
import com.app.afridge.dom.enums.SyncState;
import com.app.afridge.dom.json.FridgeItemDeserializer;
import com.app.afridge.dom.json.FridgeItemTypeAdapter;
import com.app.afridge.dom.json.NoteItemDeserializer;
import com.app.afridge.dom.json.NoteItemTypeAdapter;
import com.app.afridge.interfaces.ReplicationListener;
import com.app.afridge.ui.MainActivity;
import com.app.afridge.utils.Common;
import com.app.afridge.utils.Log;
import com.app.afridge.utils.SharedPrefStore;
import com.cloudant.sync.datastore.BasicDocumentRevision;
import com.cloudant.sync.datastore.ConflictException;
import com.cloudant.sync.datastore.Datastore;
import com.cloudant.sync.datastore.DatastoreManager;
import com.cloudant.sync.datastore.DatastoreNotCreatedException;
import com.cloudant.sync.datastore.DocumentBodyFactory;
import com.cloudant.sync.datastore.DocumentException;
import com.cloudant.sync.datastore.DocumentNotFoundException;
import com.cloudant.sync.datastore.MutableDocumentRevision;
import com.cloudant.sync.replication.PullReplication;
import com.cloudant.sync.replication.PushReplication;
import com.cloudant.sync.replication.Replication;
import com.cloudant.sync.replication.Replicator;
import com.cloudant.sync.replication.ReplicatorFactory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import de.greenrobot.event.EventBus;


/**
 * Cloudant synchronization
 * <p/>
 * Created by drakuwa on 4/8/15.
 */
public class CloudantService {

    // dataStore constants
    private static final String DATASTORE_MANGER_DIR = "data";

    private static final String DATASTORE_NAME = "fridge_datastore";

    // cloudant constants
    private static final String CLOUDANT_HOST = "fridge.cloudant.com";

    private static final String CLOUDANT_DATABASE = "fridge";

    private static final String CLOUDANT_API_KEY = "thersendressionsedinglos";

    private static final String CLOUDANT_API_SECRET = "iWVUbOhCj1bBjHWnE2RjK4cC";

    // Singleton
    public static volatile CloudantService instance = null;

    public static SyncState STATE = SyncState.IDLE;

    private final Context context;

    private final AuthState authState;

    private Datastore dataStore;

    private Replicator pushReplicator;

    private Replicator pullReplicator;

    /**
     * CloudantService constructor
     *
     * @param context Context instance
     */
    public CloudantService(Context context) {

        if (context == null) {
            throw new IllegalArgumentException("Context must not be null.");
        }
        // set the context
        this.context = context.getApplicationContext();
        Gson gson = new GsonBuilder().create();
        SharedPrefStore prefStore = SharedPrefStore.load(this.context);
        this.authState = AuthState.load(gson, prefStore);

        // initialize the DataStore
        initDataStore();
    }

    /**
     * Get instance of CloudantService
     *
     * @param context Context object
     * @return an instance of CloudantService
     */
    public static CloudantService with(Context context) {

        if (instance == null) {
            synchronized (CloudantService.class) {
                if (instance == null) {
                    instance = new CloudantService(context);
                }
            }
        }
        return instance;
    }

    private void initDataStore() {
        // Create a DataStoreManager using application internal storage path
        File path = context.getDir(DATASTORE_MANGER_DIR, Context.MODE_MULTI_PROCESS);
        DatastoreManager manager = new DatastoreManager(path.getAbsolutePath());

        // Set up our tasks dataStore within its own folder in the applications
        // data directory.
        try {
            dataStore = manager.openDatastore(DATASTORE_NAME);
        } catch (DatastoreNotCreatedException e) {
            e.printStackTrace();
        }

        Log.d(Log.TAG, "Set up database at " + path.getAbsolutePath());

        // Set up the replicator objects from the app's settings.
        try {
            reloadReplicationSettings(authState.getUser());
        } catch (URISyntaxException e) {
            Log.e(Log.TAG, "Unable to construct remote URI from configuration: " + e
                    .getLocalizedMessage());
        }
    }

    /**
     * Creates a Cloudant {@link User}, assigning an ID.
     *
     * @param user our local user
     * @return new revision of the document
     */
    public User createDocument(User user) {

        MutableDocumentRevision rev = new MutableDocumentRevision();
        rev.body = DocumentBodyFactory.create(user.asMap());
        rev.docId = user.getId();
        try {
            BasicDocumentRevision created = dataStore.createDocumentFromRevision(rev);
            return User.fromRevision(created);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Updates a {@link User} document within the dataStore.
     *
     * @param user user that we need to update
     * @return the updated revision of the User
     * @throws ConflictException if the user passed in has a rev which doesn't
     *                           match the current rev in the dataStore.
     */
    public User updateDocument(User user) throws ConflictException {

        MutableDocumentRevision rev = user.getDocumentRevision().mutableCopy();
        rev.body = DocumentBodyFactory.create(user.asMap());
        rev.docId = user.getId();
        try {
            BasicDocumentRevision updated = dataStore.updateDocumentFromRevision(rev);
            return User.fromRevision(updated);
        } catch (DocumentException de) {
            return null;
        }
    }

    /**
     * Deletes a {@link User} document within the dataStore.
     *
     * @param user user to delete
     * @throws ConflictException if the user passed in has a rev which doesn't
     *                           match the current rev in the dataStore.
     */
    public void deleteDocument(User user) throws ConflictException {

        dataStore.deleteDocumentFromRevision(user.getDocumentRevision());
    }

    /**
     * Delete all saved documents
     */
    public void deleteAllDocuments() {
        int nDocs = dataStore.getDocumentCount();
        List<BasicDocumentRevision> all = dataStore.getAllDocuments(0, nDocs, true);

        // Filter all documents down to those of type User.
        for (BasicDocumentRevision rev : all) {
            try {
                dataStore.deleteDocumentFromRevision(rev);
            } catch (ConflictException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Returns all {@link User} documents in the dataStore.</p>
     */
    //    public List<User> allDocuments() {
    //
    //        int nDocs = dataStore.getDocumentCount();
    //        List<BasicDocumentRevision> all = dataStore.getAllDocuments(0, nDocs, true);
    //        List<User> users = new ArrayList<>();
    //
    //        // Filter all documents down to those of type User.
    //        for (BasicDocumentRevision rev : all) {
    //            User user = User.fromRevision(rev);
    //            if (user != null) {
    //                users.add(user);
    //            }
    //        }
    //        return users;
    //    }

    /**
     * Get the User by the given ID
     *
     * @param userId unique User ID
     * @return User object
     * @throws DocumentNotFoundException if there is no User with the given ID
     */
    public User getUserById(String userId) throws DocumentNotFoundException {
        BasicDocumentRevision retrieved = dataStore.getDocument(userId);
        return User.fromRevision(retrieved);
    }

    /**
     * <p>Starts the configured push replication.</p>
     */
    public void startPushReplication() {

        if (pushReplicator != null) {
            // Use a CountDownLatch to provide a lightweight way to wait for completion
            CountDownLatch latch = new CountDownLatch(1);
            ReplicationListener listener = new ReplicationListener(latch);
            pushReplicator.getEventBus().register(listener);
            pushReplicator.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d("InterruptedException in PUSH");
            }
            pushReplicator.getEventBus().unregister(listener);
            if (pushReplicator.getState() != Replicator.State.COMPLETE) {
                STATE = SyncState.FAILED;
                Log.d("Error replicating TO remote");
                System.out.println(listener.error);
                // post sync event
                EventBus.getDefault().post(new SyncEvent(STATE));
                // publish the broadcast
                context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));
            } else {
                STATE = SyncState.SUCCESS;
                Log.d("Success replicating TO remote");
                // post sync event
                EventBus.getDefault().post(new SyncEvent(STATE));
                // publish the broadcast
                context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));

                // Maybe delete the local document, since we only need it for syncing,
                // and with removing it, we may avoid conflicts? Or because we only
                // update it after a sync, we will always have a newer version on the web?
                // Test with multiple phones!
            }
        } else {
            STATE = SyncState.FAILED;
            EventBus.getDefault().post(new SyncEvent(STATE));
            // publish the broadcast
            context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));
            throw new RuntimeException("Push replication not set up correctly");
        }
    }

    /**
     * <p>Starts the configured pull replication.</p>
     */
    public void startPullReplication(AuthState authState) {

        if (pullReplicator != null) {
            // Use a CountDownLatch to provide a lightweight way to wait for completion
            CountDownLatch latch = new CountDownLatch(1);
            ReplicationListener listener = new ReplicationListener(latch);
            pullReplicator.getEventBus().register(listener);
            pullReplicator.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d("InterruptedException in PULL");
            }
            pullReplicator.getEventBus().unregister(listener);
            if (pullReplicator.getState() != Replicator.State.COMPLETE) {
                STATE = SyncState.FAILED;
                Log.d("Error replicating FROM remote");
                System.out.println(listener.error);
                // post sync event
                EventBus.getDefault().post(new SyncEvent(STATE));
                // publish the broadcast
                context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));
            } else {
                Log.d("Success replicating FROM remote");
                // the replication is successful, sync with local DB
                if (authState.isAuthenticated()) {
                    try {
                        User userDocument = getUserById(authState.getUser().getId());
                        if (userDocument.getId() == null) {
                            throw new DocumentNotFoundException("empty revision!?");
                        }
                        Log.d("DATASTORE: " + userDocument.toString());
                        // now that we have the pulled user info, sync it!
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(FridgeItem.class, new FridgeItemTypeAdapter())
                                .registerTypeAdapter(FridgeItem.class, new FridgeItemDeserializer())
                                .registerTypeAdapter(NoteItem.class, new NoteItemTypeAdapter())
                                .registerTypeAdapter(NoteItem.class, new NoteItemDeserializer())
                                .create();
                        Type listType = new TypeToken<List<FridgeItem>>() {
                        }.getType();
                        List<FridgeItem> cloudFridgeItems = gson
                                .fromJson(userDocument.getFridgeItemsJson(), listType);
                        List<FridgeItem> dbFridgeItems = new Select()
                                .from(FridgeItem.class).execute();
                        Set<Integer> dbItemIDSet = new HashSet<>();
                        for (FridgeItem dbItem : dbFridgeItems) {
                            dbItemIDSet.add(dbItem.getItemId());
                        }
                        Log.d("dbItemIDSet: " + dbItemIDSet + " cloud size: " + cloudFridgeItems
                                .size());
                        for (FridgeItem cloudItem : cloudFridgeItems) {
                            for (FridgeItem dbItem : dbFridgeItems) {
                                // here we need to check for item ID match
                                if (cloudItem.getItemId() == dbItem.getItemId()) {
                                    Log.d("cloudItem.getItemId() == dbItem.getItemId(): "
                                            + cloudItem.getName());
                                    // now we check for timestamps
                                    if (cloudItem.getEditTimestamp() > dbItem.getEditTimestamp()) {
                                        Log.d("cloudItem.getEditTimestamp() > dbItem.getEditTimestamp(): "
                                                + cloudItem.getName());
                                        // set every updated cloud item values to the DB item
                                        dbItem.setName(cloudItem.getName());
                                        dbItem.setExpirationDate(cloudItem.getExpirationDate());
                                        dbItem.setQuantity(cloudItem.getQuantity());
                                        dbItem.setTypeOfQuantity(cloudItem.getTypeOfQuantity());
                                        dbItem.setRemoved(cloudItem.isRemoved());
                                        dbItem.setDetails(cloudItem.getDetails());
                                        dbItem.setEditTimestamp(cloudItem.getEditTimestamp());
                                        // save the type if we have a numeric string
                                        if (TextUtils.isDigitsOnly(cloudItem.getType())) {
                                            dbItem.setType(cloudItem.getType());
                                        } else {
                                            // save the image if we have a Base64 encoded string
                                            byte[] decodedString = Base64.decode(
                                                    cloudItem.getType(), Base64.DEFAULT);
                                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(
                                                    decodedString, 0, decodedString.length);
                                            File savedImageFile = Common
                                                    .storeImage(decodedBitmap, context);
                                            assert savedImageFile != null;
                                            dbItem.setType(savedImageFile.getAbsolutePath());
                                        }
                                        dbItem.save();

                                        // save the change when the item has changed
                                        if (dbItem.isRemoved()) {
                                            HistoryItem historyItem = new HistoryItem(dbItem,
                                                    dbItem.getEditTimestamp() == 0 ? Calendar
                                                            .getInstance().getTimeInMillis() / 1000
                                                            : dbItem.getEditTimestamp() / 1000,
                                                    ChangeType.DELETE);
                                            historyItem.save();
                                        } else {
                                            HistoryItem historyItem = new HistoryItem(dbItem,
                                                    dbItem.getEditTimestamp() == 0 ? Calendar
                                                            .getInstance().getTimeInMillis() / 1000
                                                            : dbItem.getEditTimestamp() / 1000,
                                                    ChangeType.MODIFY);
                                            historyItem.save();
                                        }
                                    }
                                }
                            }
                            if (!dbItemIDSet.contains(cloudItem.getItemId())) {
                                // if the local list doesn't have the cloud item, save it
                                Log.d("!dbItemIDSet.contains(cloudItem.getItemId()): "
                                        + cloudItem.getName());
                                FridgeItem dbItem = new FridgeItem();
                                dbItem.setItemId(cloudItem.getItemId());
                                dbItem.setName(cloudItem.getName());
                                dbItem.setExpirationDate(cloudItem.getExpirationDate());
                                dbItem.setQuantity(cloudItem.getQuantity());
                                dbItem.setTypeOfQuantity(cloudItem.getTypeOfQuantity());
                                dbItem.setRemoved(cloudItem.isRemoved());
                                dbItem.setDetails(cloudItem.getDetails());
                                dbItem.setEditTimestamp(cloudItem.getEditTimestamp());
                                // save the type if we have a numeric string
                                if (TextUtils.isDigitsOnly(cloudItem.getType())) {
                                    dbItem.setType(cloudItem.getType());
                                } else {
                                    // save the image if we have a Base64 encoded string
                                    byte[] decodedString = Base64.decode(
                                            cloudItem.getType(), Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(
                                            decodedString, 0, decodedString.length);
                                    File savedImageFile = Common
                                            .storeImage(decodedBitmap, context);
                                    assert savedImageFile != null;
                                    dbItem.setType(savedImageFile.getAbsolutePath());
                                }
                                Log.d(dbItem.toString());
                                dbItem.save();

                                // add the saved item to history
                                HistoryItem historyItem = new HistoryItem(dbItem,
                                        dbItem.getEditTimestamp() == 0 ? Calendar
                                                .getInstance().getTimeInMillis() / 1000
                                                : dbItem.getEditTimestamp() / 1000,
                                        ChangeType.ADD);
                                historyItem.save();
                            }
                        }

                        // sync the notes
                        Type notesListType = new TypeToken<List<NoteItem>>() {
                        }.getType();
                        List<NoteItem> cloudNoteItems = gson
                                .fromJson(userDocument.getNoteItemsJson(), notesListType);
                        List<NoteItem> dbNoteItems = new Select().from(NoteItem.class).execute();
                        Set<Integer> dbNoteItemIDSet = new HashSet<>();
                        for (NoteItem dbItem : dbNoteItems) {
                            dbNoteItemIDSet.add(dbItem.getItemId());
                        }
                        Log.d("dbNoteItemIDSet: " + dbNoteItemIDSet + " cloud size: "
                                + cloudNoteItems.size());

                        for (NoteItem cloudItem : cloudNoteItems) {
                            for (NoteItem dbItem : dbNoteItems) {
                                // here we need to check for item ID match
                                if (cloudItem.getItemId() == dbItem.getItemId()) {
                                    // now we check for timestamps
                                    if (cloudItem.getTimestamp() > dbItem.getTimestamp()) {
                                        dbItem.setNote(cloudItem.getNote());
                                        dbItem.setChecked(cloudItem.isChecked());
                                        dbItem.setTimestamp(cloudItem.getTimestamp());
                                        dbItem.save();
                                    }
                                }
                            }
                            if (!dbNoteItemIDSet.contains(cloudItem.getItemId())) {
                                // if the local list doesn't have the cloud item, save it
                                Log.d("!dbNoteItemIDSet.contains(cloudItem.getItemId()): "
                                        + cloudItem.getNote());
                                NoteItem dbItem = new NoteItem();
                                dbItem.setItemId(cloudItem.getItemId());
                                dbItem.setNote(cloudItem.getNote());
                                dbItem.setChecked(cloudItem.isChecked());
                                dbItem.setTimestamp(cloudItem.getTimestamp());
                                dbItem.save();
                                Log.d(dbItem.toString());
                            }
                        }

                        // now we need to update and save the User document in the dataStore
                        userDocument.setFridgeItemsJson(ItemList.getItemList());
                        List<FridgeItem> noteItems = new Select()
                                .from(NoteItem.class).execute();
                        userDocument.setNoteItemsJson(gson.toJson(noteItems));
                        updateDocument(userDocument);

                        // after we've updated the local document, the sync has finished and we need to push the changes
                        startPushReplication();
                    } catch (DocumentNotFoundException e) {
                        Log.d("DocumentNotFoundException: " + e.getLocalizedMessage());
                        e.printStackTrace();
                        // there was no local User document, so we create one
                        createDocument(authState.getUser());
                        // ...and push the changes
                        startPushReplication();
                    } catch (ConflictException e) {
                        STATE = SyncState.FAILED;
                        EventBus.getDefault().post(new SyncEvent(STATE));
                        // publish the broadcast
                        context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));
                        Log.d("ConflictException: " + e.getLocalizedMessage());
                        e.printStackTrace();
                    } catch (NullPointerException npe) {
                        STATE = SyncState.FAILED;
                        EventBus.getDefault().post(new SyncEvent(STATE));
                        // publish the broadcast
                        context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));
                    }
                } else {
                    STATE = SyncState.FAILED;
                    EventBus.getDefault().post(new SyncEvent(STATE));
                    // publish the broadcast
                    context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));
                }
            }
        } else {
            STATE = SyncState.FAILED;
            // publish the broadcast
            context.sendBroadcast(new Intent(MainActivity.ACTION_FINISHED_SYNC));
            throw new RuntimeException("Push replication not set up correctly");
        }
    }

    /**
     * Synchronization service:
     * curl -H "Authorization: Basic ZnJpZGdlOkxlbW9uY2hhaXI3NDg="  https://fridge.cloudant.com/fridge
     *
     * First start pull sync and it will get the web contents of the document with docID = userId
     * Then we sync with the local database if there was a remote document, if not, we create the
     * local
     * document. In the end we push the updated local document no matter what
     */
    public void startSynchronization(AuthState authState) {
        if (pullReplicator != null && pullReplicator.getState() != Replicator.State.STARTED
                && pushReplicator != null
                && pushReplicator.getState() != Replicator.State.STARTED) {
            STATE = SyncState.SYNCING;
            // publish the broadcast
            context.sendBroadcast(new Intent(MainActivity.ACTION_STARTED_SYNC));
            startPullReplication(authState);
        } else {
            STATE = SyncState.IDLE;
            EventBus.getDefault().post(new SyncEvent(STATE));
        }
    }

    /**
     * <p>Stops running replications and reloads the replication settings from
     * the app's preferences.</p>
     *
     * @param user the authenticated user
     */
    public void reloadReplicationSettings(User user)
            throws URISyntaxException {

        // Stop running replications before reloading the replication
        // settings.
        // The stop() method instructs the replicator to stop ongoing
        // processes, and to stop making changes to the datastore. Therefore,
        // we don't clear the listeners because their complete() methods
        // still need to be called once the replications have stopped
        // for the UI to be updated correctly with any changes made before
        // the replication was stopped.
        stopAllReplications();

        // Set up the new replicator objects
        URI uri = getServerURI();

        PushReplication push = new PushReplication();
        push.source = dataStore;
        push.target = uri;

        pushReplicator = ReplicatorFactory.oneway(push);

        // get the user ID filter
        Map<String, String> parameters = new HashMap<>();
        parameters.put("id", user.getId()); // _id should IS the docId
        Replication.Filter filter = new Replication.Filter("filterDoc/FILTER", parameters);

        PullReplication pull = new PullReplication();
        pull.source = uri;
        pull.target = dataStore;
        pull.filter = filter;

        pullReplicator = ReplicatorFactory.oneway(pull);

        Log.d("reloadReplicationSettings authState.getUser().getId(): " + user.getId());
        Log.d(Log.TAG, "Set up replicators for URI:" + uri.toString());
    }

    /**
     * <p>Stops running replications.</p>
     * <p>The stop() methods stops the replications asynchronously, see the
     * replicator docs for more information.</p>
     */
    public void stopAllReplications() {

        if (pullReplicator != null) {
            pullReplicator.stop();
        }
        if (pushReplicator != null) {
            pushReplicator.stop();
        }
    }

    /**
     * <p>Returns the URI for the remote database, based on the app's
     * configuration.</p>
     *
     * @return the remote database's URI
     * @throws URISyntaxException if the settings give an invalid URI
     */
    private URI getServerURI() throws URISyntaxException {

        // We recommend always using HTTPS to talk to Cloudant.
        return new URI("https", CLOUDANT_API_KEY + ":" + CLOUDANT_API_SECRET,
                CLOUDANT_HOST, 443, "/" + CLOUDANT_DATABASE, null, null);
    }
}
