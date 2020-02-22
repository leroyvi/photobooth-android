/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.chameleooo.photobooth.ptp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;

import net.chameleooo.photobooth.R;
import net.chameleooo.photobooth.util.NotificationIds;

public class WorkerNotifier implements Camera.WorkerListener {

    private final NotificationManager notificationManager;
    private final Notification notification;
    private final int uniqueId;

    public WorkerNotifier(Context context) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        uniqueId = NotificationIds.getInstance().getUniqueIdentifier(WorkerNotifier.class.getName() + ":running");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        notification = builder.setContentIntent(null)
                .setSmallIcon(R.drawable.icon).setTicker(context.getString(R.string.worker_ticker)).setWhen(0)
                .setAutoCancel(true).setContentTitle(context.getString(R.string.worker_ticker))
                .setContentText(context.getString(R.string.worker_content_text)).build();
        notificationManager.notify(uniqueId, notification);
    }

    @Override
    public void onWorkerStarted() {
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(uniqueId, notification);
    }

    @Override
    public void onWorkerEnded() {
        notificationManager.cancelAll();
    }

}
