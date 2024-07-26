package com.cometchat.kotlinsampleapp.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager.BadTokenException
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.cometchat.calls.core.CallAppSettings.CallAppSettingBuilder
import com.cometchat.calls.core.CometChatCalls
import com.cometchat.chat.constants.CometChatConstants
import com.cometchat.chat.core.Call
import com.cometchat.chat.core.CometChat
import com.cometchat.chat.core.CometChat.CallbackListener
import com.cometchat.chat.exceptions.CometChatException
import com.cometchat.chat.models.BaseMessage
import com.cometchat.chat.models.Group
import com.cometchat.chat.models.User
import com.cometchat.chatuikit.calls.CallingExtension
import com.cometchat.chatuikit.calls.CallingExtensionDecorator
import com.cometchat.chatuikit.calls.CometChatCallActivity
import com.cometchat.chatuikit.calls.callbutton.CometChatCallButtons
import com.cometchat.chatuikit.shared.cometchatuikit.CometChatUIKit
import com.cometchat.chatuikit.shared.cometchatuikit.UIKitSettings.UIKitSettingsBuilder
import com.cometchat.chatuikit.shared.framework.ChatConfigurator
import com.cometchat.chatuikit.shared.framework.DataSource
import com.cometchat.chatuikit.shared.models.CometChatMessageOption
import com.cometchat.chatuikit.shared.resources.theme.CometChatTheme
import com.cometchat.chatuikit.shared.resources.utils.Utils
import com.cometchat.chatuikit.shared.views.button.ButtonStyle
import com.cometchat.kotlinsampleapp.AppConstants
import com.cometchat.kotlinsampleapp.AppUtils
import com.cometchat.kotlinsampleapp.AppUtils.Companion.fetchDefaultObjects
import com.cometchat.kotlinsampleapp.BuildConfig
import com.cometchat.kotlinsampleapp.R
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var user1: MaterialCardView
    private lateinit var user2: MaterialCardView
    private lateinit var user3: MaterialCardView
    private lateinit var user4: MaterialCardView
    private lateinit var ivLogo: AppCompatImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCometChat: AppCompatTextView
    private lateinit var parentView: LinearLayout
    private lateinit var gridLayoutContainer: LinearLayout
    private lateinit var stateMessage: TextView
    private lateinit var stateLayout: LinearLayout
    private lateinit var user1Name: TextView
    private lateinit var user2Name: TextView
    private lateinit var user3Name: TextView
    private lateinit var user4Name: TextView
    private lateinit var user1Avatar: ImageView
    private lateinit var user2Avatar: ImageView
    private lateinit var user3Avatar: ImageView
    private lateinit var user4Avatar: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callAppSettings = CallAppSettingBuilder()
            .setAppId(AppConstants.APP_ID)
            .setRegion(AppConstants.REGION)
            .build()
        CometChatCalls.init(
            this,
            callAppSettings,
            object : CometChatCalls.CallbackListener<String>() {
                override fun onSuccess(successMessage: String) {
                    // The code below was created for workaround
                    // The actual library HAS NO SUPPORT for group call
                    return ChatConfigurator.enable { dataSource: DataSource? ->
                        object : CallingExtensionDecorator(
                            dataSource,
                        ) {
                            fun initiateCall(group: Group?) {
                                if (group == null) {
                                    // Don't care about non group call, just throw an error
                                    TODO()
                                }

                                if (CometChat.getActiveCall() != null || CallingExtension.getActiveCall() != null) {
                                    return
                                }

                                val call = Call(group.guid, CometChatConstants.RECEIVER_TYPE_GROUP, CometChatConstants.CALL_TYPE_AUDIO)
                                val jsonObject = JSONObject()
                                try {
                                    jsonObject.put("bookingId", 6)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                call.metadata = jsonObject

                                CometChat.initiateCall(call, object : CallbackListener<Call>() {
                                    override fun onSuccess(call: Call) {
                                        if (call.callReceiver is Group) {
                                            CometChatCallActivity.launchConferenceCallScreen(
                                                this@MainActivity,
                                                call,
                                                null,
                                            )
                                        }
                                    }

                                    override fun onError(e: CometChatException) {
                                        // Dont care
                                    }
                                })
                            }

                            override fun getCommonOptions(
                                context: Context?,
                                baseMessage: BaseMessage?,
                                group: Group?
                            ): MutableList<CometChatMessageOption> {
                                val options = super.getCommonOptions(context, baseMessage, group)
                                return options.filter { it.id !in setOf("message_privately", "share", "reply_in_thread", "delete", "message_information", "edit") }.toMutableList()
                            }

                            override fun getMessageOptions(
                                context: Context?,
                                baseMessage: BaseMessage?,
                                group: Group?
                            ): MutableList<CometChatMessageOption> {
                                val options = super.getTextMessageOptions(context, baseMessage, group)
                                return options.filter { it.id !in setOf("message_privately", "share", "reply_in_thread", "delete", "message_information", "edit") }.toMutableList()
                            }

                            override fun getAuxiliaryHeaderMenu(
                                context: Context,
                                user: User?,
                                group: Group?
                            ): View {
                                val presentView = super.getAuxiliaryHeaderMenu(context, user, group)
                                val linearLayout = presentView as LinearLayout
                                linearLayout.removeViewAt(0)

                                val callButtons = object : CometChatCallButtons(context) {}.apply {
                                    videoCallButton.hideButtonBackground(true)
                                    voiceCallButton.hideButtonBackground(true)
                                    setVideoCallIcon(com.cometchat.chatuikit.R.drawable.cometchat_video_icon)
                                    setVoiceCallIcon(com.cometchat.chatuikit.R.drawable.cometchat_call_icon)
                                    hideButtonText(true)
                                    setMarginForButtons(
                                        Utils.convertDpToPx(
                                            context,
                                            1
                                        )
                                    )
                                    setButtonStyle(
                                        ButtonStyle().setButtonIconTint(
                                            CometChatTheme.getInstance().palette.getPrimary(context)
                                        )
                                    )
                                    hideVoiceCall(false)
                                    hideVideoCall(true)

                                    findViewById<View>(com.cometchat.chatuikit.R.id.voice_call).setOnClickListener {
                                        initiateCall(group)
                                    }
                                }
                                linearLayout.addView(callButtons, 0)

                                return linearLayout
                            }
                        }
                    }
                }

                override fun onError(error: com.cometchat.calls.exceptions.CometChatException) {
                    // Dont care
                }
            }
        )

        val uiKitSettings =
            UIKitSettingsBuilder().setRegion(AppConstants.REGION).setAppId(AppConstants.APP_ID)
                .subscribePresenceForAllUsers().build()
        CometChatUIKit.init(this, uiKitSettings, object : CometChat.CallbackListener<String?>() {
            override fun onSuccess(s: String?) {
                CometChat.setDemoMetaInfo(appMetadata)
                if (CometChatUIKit.getLoggedInUser() != null) {
                    fetchDefaultObjects()
                    startActivity(
                        Intent(
                            this@MainActivity,
                            HomeActivity::class.java
                        )
                    )
                    finish()
                } else {
                    viewInit()
                }
            }

            override fun onError(e: CometChatException) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun viewInit() {
        setContentView(R.layout.activity_main)
        parentView = findViewById(R.id.parent_view)
        progressBar = findViewById(R.id.progress_bar)
        stateMessage = findViewById(R.id.state_message)
        stateLayout = findViewById(R.id.state_layout)
        gridLayoutContainer = findViewById(R.id.grid_layout_container)
        user1 = findViewById(R.id.user1)
        user2 = findViewById(R.id.user2)
        user3 = findViewById(R.id.user3)
        user4 = findViewById(R.id.user4)
        ivLogo = findViewById(R.id.ivLogo)
        tvCometChat = findViewById(R.id.tvComet)
        user1Name = findViewById(R.id.user1_name)
        user2Name = findViewById(R.id.user2_name)
        user3Name = findViewById(R.id.user3_name)
        user4Name = findViewById(R.id.user4_name)
        user1Avatar = findViewById(R.id.user1_avatar_image)
        user2Avatar = findViewById(R.id.user2_avatar_image)
        user3Avatar = findViewById(R.id.user3_avatar_image)
        user4Avatar = findViewById(R.id.user4_avatar_image)

        user1.visibility = View.GONE
        user2.visibility = View.GONE
        user3.visibility = View.GONE
        user4.visibility = View.GONE

        gridLayoutContainer.visibility = View.INVISIBLE
        stateMessage.text = getString(R.string.please_wait)
        progressBar.visibility = View.VISIBLE
        Utils.setStatusBarColor(this, resources.getColor(android.R.color.white))
        AppUtils.fetchSampleUsers(object : CometChat.CallbackListener<List<User>>() {
            override fun onSuccess(users: List<User>) {
                if (users.isNotEmpty()) {
                    setUsers(users)
                } else {
                    stateLayout.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    stateMessage.text = getString(R.string.no_sample_users_available)
                }
            }

            override fun onError(e: CometChatException) {
                setUsers(AppUtils.processSampleUserList(AppUtils.loadJSONFromAsset(this@MainActivity)))
            }
        })
        findViewById<View>(R.id.login).setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    LoginActivity::class.java
                )
            )
        }

        user1.setOnClickListener {
            findViewById<View>(R.id.user1Progressbar).visibility = View.VISIBLE
            login(user1.tag.toString())
        }
        user2.setOnClickListener {
            findViewById<View>(R.id.user2Progressbar).visibility = View.VISIBLE
            login(user2.tag.toString())
        }
        user3.setOnClickListener {
            findViewById<View>(R.id.user3Progressbar).visibility = View.VISIBLE
            login(user3.tag.toString())
        }
        user4.setOnClickListener {
            findViewById<View>(R.id.user4Progressbar).visibility = View.VISIBLE
            login(user4.tag.toString())
        }

        if (Utils.isDarkMode(this)) {
            ivLogo.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
        } else {
            ivLogo.imageTintList =
                ColorStateList.valueOf(resources.getColor(com.cometchat.chatuikit.R.color.cometchat_primary_text_color))
        }
        setUpUI()
    }

    private fun setUsers(users: List<User>) {
        progressBar.visibility = View.GONE
        stateLayout.visibility = View.GONE
        gridLayoutContainer.visibility = View.VISIBLE
        for (i in users.indices) {
            if (i == 0) {
                user1Name.text = users[i].name
                Glide.with(this).load(users[i].avatar).error(R.drawable.ironman)
                    .into(user1Avatar)
                user1.tag = users[i].uid
                user1.visibility = View.VISIBLE
            } else if (i == 1) {
                user2Name.text = users[i].name
                Glide.with(this).load(users[i].avatar).error(R.drawable.captainamerica)
                    .into(user2Avatar)
                user2.tag = users[i].uid
                user2.visibility = View.VISIBLE
            } else if (i == 2) {
                user3Name.text = users[i].name
                Glide.with(this).load(users[i].avatar).error(R.drawable.spiderman)
                    .into(user3Avatar)
                user3.tag = users[i].uid
                user3.visibility = View.VISIBLE
            } else if (i == 3) {
                user4Name.text = users[i].name
                Glide.with(this).load(users[i].avatar).error(R.drawable.wolverine)
                    .into(user4Avatar)
                user4.tag = users[i].uid
                user4.visibility = View.VISIBLE
            }
        }
    }

    private fun login(uid: String) {
        CometChatUIKit.login(uid, object : CometChat.CallbackListener<User?>() {
            override fun onSuccess(user: User?) {
                fetchDefaultObjects()
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            }

            override fun onError(e: CometChatException) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setUpUI() {
        if (AppUtils.isNightMode(this)) {
            Utils.setStatusBarColor(
                this, ContextCompat.getColor(
                    this, R.color.app_background_dark
                )
            )
            parentView.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_background_dark))
            tvCometChat.setTextColor(resources.getColor(R.color.app_background))
        } else {
            Utils.setStatusBarColor(this, resources.getColor(R.color.app_background))
            parentView.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.app_background))
            tvCometChat.setTextColor(resources.getColor(R.color.app_background_dark))
        }
    }

    private val appMetadata: JSONObject
        get() {
            val jsonObject = JSONObject()
            try {
                jsonObject.put("name", resources.getString(R.string.app_name))
                jsonObject.put("type", "sample")
                jsonObject.put("version", BuildConfig.VERSION_NAME)
                jsonObject.put("bundle", BuildConfig.APPLICATION_ID)
                jsonObject.put("platform", "android")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return jsonObject
        }

    fun createUser(view: View?) {
        startActivity(Intent(this, CreateUserActivity::class.java))
    }
}