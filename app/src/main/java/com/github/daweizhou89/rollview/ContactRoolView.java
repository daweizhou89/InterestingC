
package com.github.daweizhou89.rollview;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RemoteViews.RemoteView;

import com.github.daweizhou89.rollview.utils.StringUtils;


@RemoteView
public class ContactRoolView extends RollView {

	private static final boolean DEBUG_LOG = false;
	private static final String TAG = "dawei";
	private static final int LIMITED_NAME_LENGTH = 3;
    private static final String[] TEXT_LOADING_TIPS = new String[] {"加载中.  ", "加载中.. ", "加载中..."};
    private static final String TEXT_NOCONTACT_TIPS = "无联系人";
    
    private ListTypeRollViewAdapter mAdapter;
    private ContactLoadingTask mContactLoadingTask;
    private Handler mHandler;
    
    ContentObserver mContactObserver;
    
    public ContactRoolView(Context context) {
        this(context, null);
        
    }
    
    public ContactRoolView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ContactRoolView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        init();
    }
    
    public void init() {
        this.setBackgroundColor(Color.TRANSPARENT);
//        this.setOnClickListener(new View.OnClickListener() {
//            
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                getContext().startActivity(intent);
//            }
//        });
        
        this.addRollCellClickedListener(new IRollCellClickedListener() {
            
            @Override
            public void onRollCellClicked(float x, float y, RollCell rollCell) {
                ContactItem contactItem = (ContactItem)rollCell.obj;
                if(contactItem != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactItem.personId)));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        mAdapter = new ListTypeRollViewAdapter(null);
        this.setRollViewAdapter(mAdapter);
        mContactLoadingTask = new ContactLoadingTask();

        mHandler = new Handler();
        mContactObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                if (selfChange) {
                    if(mContactLoadingTask.isCancelled()) mContactLoadingTask.cancel(true);
                    mContactLoadingTask.execute();
                }
            }
        };
        
        mContactLoadingTask.execute();
        pause();
    }
    
    @Override
    public void pause() {
        super.pause();
        getContext().getContentResolver().registerContentObserver(Phone.CONTENT_URI, true, mContactObserver);
    }
    
    @Override
    public void resume() {
        super.resume();
        ContentResolver contentResolver = getContext().getContentResolver();
        contentResolver.unregisterContentObserver(mContactObserver);
    }
    
    private static final String[] CONTACT_ID_PROJECTION = new String[] {
            Phone.NUMBER, // 0
            Phone.LABEL, // 1
            Phone.DISPLAY_NAME, // 2
            Phone.CONTACT_ID, // 3
    };

    private static final int PHONE_NUMBER_COLUMN = 0;
    private static final int PHONE_LABEL_COLUMN = 1;
    private static final int CONTACT_NAME_COLUMN = 2;
    private static final int CONTACT_ID_COLUMN = 3;
    
    public class ContactItem {
        public int personId;
        public String name;
        public String number;
        public String label;
    }
    
    class ContactLoadingTask extends AsyncTask<Void, Void, List<ContactItem>> {

        private int mTipsIndex;
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTipsIndex = 0;
            ContactRoolView.this.setNoDataTips(TEXT_LOADING_TIPS[mTipsIndex]);
            mLastUpdateTime = System.currentTimeMillis();
        }
        
        @Override
        protected List<ContactItem> doInBackground(Void... params) {
            List<ContactItem> contactItems = null;
            Cursor cursor = null;
            try {
                cursor = getContext().getContentResolver().query(
                        Phone.CONTENT_URI, CONTACT_ID_PROJECTION, null,
                        null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    contactItems = new ArrayList<ContactItem>();
                    do {
                        ContactItem contactItem = new ContactItem();
                        contactItem.number = cursor.getString(PHONE_NUMBER_COLUMN);
                        contactItem.label = cursor.getString(PHONE_LABEL_COLUMN);
                        contactItem.name = StringUtils.getEllipsis(cursor.getString(CONTACT_NAME_COLUMN), LIMITED_NAME_LENGTH);
                        contactItem.personId = cursor.getInt(CONTACT_ID_COLUMN);
                        if(null != contactItem.number && !"".equals(contactItem.number)) {
                            contactItems.add(contactItem);
                        }
                        publishProgress();
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                if (DEBUG_LOG) Log.e(TAG, "", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return contactItems;
        }
        
        private long mLastUpdateTime;
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            long deltaTime = System.currentTimeMillis() - mLastUpdateTime;
            if(deltaTime > 500) {
                ++mTipsIndex;
                if(mTipsIndex >= TEXT_LOADING_TIPS.length) {
                    mTipsIndex = 0;
                }
                ContactRoolView.this.setNoDataTips(TEXT_LOADING_TIPS[mTipsIndex]);
                mLastUpdateTime = System.currentTimeMillis();
            }
        }
        
        @Override
        protected void onPostExecute(List<ContactItem> result) {
            super.onPostExecute(result);
            if(result != null) {
                contactsLoaded(result);
            } else {
                ContactRoolView.this.setNoDataTips(TEXT_NOCONTACT_TIPS);
            }
        }
    }
    
    private void contactsLoaded(List<ContactItem> contactItems) {
        mAdapter.setContactItems(contactItems);
        mAdapter.notifyDataChanged(true);
    }
    
    class ListTypeRollViewAdapter extends RollViewAdapter {
        
        private List<ContactItem> mContactItems;
        
        public ListTypeRollViewAdapter(List<ContactItem> contactItems) {
            mContactItems = contactItems;
        }
        
        public void setContactItems(List<ContactItem> contactItems) {
            mContactItems = contactItems;
        }
                
        private int mIndex = 0;
        
        @Override
        public int getCount() {
            int ret = 0;
            if(mContactItems != null) ret = mContactItems.size();
            return ret;
        }
        
        @Override
        public boolean isUniformAndCircular() {
            return false;
        }
        
        @Override
        public int getTextColor() {
            return Color.WHITE;
        }
        
        @Override
        public List<RollCell> bindCells() {
            if(mContactItems == null) return null;
            List<RollCell> rollCells = new ArrayList<RollCell>();
            int slices = getSlices();
            int rollcellCount = 2 * slices * slices;
            for (int i = 0; i < rollcellCount; i++) {
                RollCell rollCell = new RollCell();
                rollCell.name = mContactItems.get(mIndex).name;
                rollCell.obj = mContactItems.get(mIndex);
                rollCells.add(rollCell);
                if (++mIndex >= getCount()) {
                    mIndex = 0;
                }
            }
            return rollCells;
        }

        @Override
        public RollCell bindNextCell(RollCell rollCell) {
            if(mContactItems == null || rollCell == null) return null;
            rollCell.name = mContactItems.get(mIndex).name;
            rollCell.obj = mContactItems.get(mIndex);
            if (++mIndex >= getCount()) {
                mIndex = 0;
            }
            return rollCell;
        }
    }
}
