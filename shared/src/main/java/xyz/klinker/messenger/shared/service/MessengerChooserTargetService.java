/*
 * Copyright (C) 2016 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger.shared.service;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;

import java.util.ArrayList;
import java.util.List;

import xyz.klinker.messenger.shared.data.DataSource;
import xyz.klinker.messenger.shared.data.model.Conversation;
import xyz.klinker.messenger.shared.util.DensityUtil;
import xyz.klinker.messenger.shared.util.ImageUtils;

@TargetApi(Build.VERSION_CODES.M)
public class MessengerChooserTargetService extends ChooserTargetService {

    public static final String EXTRA_CONVO_ID = "chooser_target_convo_id";

    @Override
    public List<ChooserTarget> onGetChooserTargets(ComponentName componentName,
                                                   IntentFilter intentFilter) {
        List<ChooserTarget> targets = new ArrayList<>();

        DataSource source = DataSource.getInstance(this);
        source.open();
        Cursor cursor = source.getPinnedConversations();

        if (cursor == null || cursor.getCount() == 0) {
            try {
                cursor.close();
            } catch (Exception e) { }

            cursor = source.getUnarchivedConversations();
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                targets.add(createTarget(cursor, componentName));
            } while (cursor.moveToNext() && targets.size() < 5);

            cursor.close();
        }

        source.close();
        return targets;
    }

    private ChooserTarget createTarget(Cursor cursor, ComponentName componentName) {
        final String targetName = cursor.getString(
                cursor.getColumnIndex(Conversation.COLUMN_TITLE));
        Bitmap image = ImageUtils.clipToCircle(ImageUtils.getBitmap(this, cursor.getString(
                cursor.getColumnIndex(Conversation.COLUMN_IMAGE_URI))));

        Bitmap color = Bitmap.createBitmap(DensityUtil.toDp(this, 48), DensityUtil.toDp(this, 48), Bitmap.Config.ARGB_8888);
        color.eraseColor(cursor.getInt(cursor.getColumnIndex(Conversation.COLUMN_COLOR)));
        color = ImageUtils.clipToCircle(color);

        final Icon targetIcon = image == null ? Icon.createWithBitmap(color) : Icon.createWithBitmap(image);
//        final float targetRanking = cursor.getCount() == 1 ? 1.0f :
//                ((float) (cursor.getCount() - cursor.getPosition() + 1) / (cursor.getCount() + 1.0f));
        final Bundle targetExtras = new Bundle();
        targetExtras.putLong(EXTRA_CONVO_ID, cursor.getLong(
                cursor.getColumnIndex(Conversation.COLUMN_ID)));

        return new ChooserTarget(targetName, targetIcon, 1.0f,
                componentName, targetExtras);

    }

}
