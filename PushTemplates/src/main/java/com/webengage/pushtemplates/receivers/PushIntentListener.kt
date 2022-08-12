package com.webengage.pushtemplates.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.webengage.pushtemplates.services.NotificationService
import com.webengage.pushtemplates.utils.Constants
import com.webengage.sdk.android.PendingIntentFactory
import com.webengage.sdk.android.actions.render.PushNotificationData
import org.json.JSONObject

class PushIntentListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals(Constants.DELETE_ACTION)) {
            dismissNotification(context!!, intent)
        }
    }

    /**
     * Dismiss the notification with the provided notification ID
     */
    private fun dismissNotificationWithId(context: Context, id: Int) {
            with(NotificationManagerCompat.from(context)) {
                this.cancel(id)
            }
        }

    /**
     * Used to listen to the DISMISS CTA button clicks
     * If LOG_DISMISS is true then the notification close event will be logged.
     * If TEMPLATE_TYPE is ProgressBar, then the NotificationService will be stopped.
     * If TEMPLATE_TYPE is ProgressBar, then the Notification will be cancelled.
     */
    private fun dismissNotification(context: Context, intent: Intent){
        if (intent.extras != null && intent.extras!!.containsKey(Constants.PAYLOAD)) {
            val pushData = PushNotificationData(intent.extras!!.getString(Constants.PAYLOAD)
                ?.let { JSONObject(it) }, context
            )

            if(intent.extras!!.containsKey(Constants.LOG_DISMISS) && intent.extras!!.getBoolean(Constants.LOG_DISMISS)){
                val dismissIntent = PendingIntentFactory.constructPushDeletePendingIntent(
                    context,
                    pushData
                )
                dismissIntent.send()
            }
            if (pushData.customData.containsKey(Constants.TEMPLATE_TYPE) && pushData.customData.getString(
                    Constants.TEMPLATE_TYPE
                ).equals(Constants.PROGRESS_BAR)
            ) {
                val notificationServiceIntent =
                    Intent(context, NotificationService::class.java)
                context.stopService(notificationServiceIntent)
            } else if (pushData.customData.containsKey(Constants.TEMPLATE_TYPE) && pushData.customData.getString(
                    Constants.TEMPLATE_TYPE
                ).equals(Constants.COUNTDOWN)
            ) {
                dismissNotificationWithId(context,pushData.variationId.hashCode())
            }
        }
    }
}