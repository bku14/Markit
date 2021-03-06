package com.markit.android.chat.files;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.markit.android.base.files.*;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.markit.android.base.files.*;
import com.markit.android.ConversationAdapter;
import com.markit.android.ItemDetail;
import com.markit.android.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.markit.android.R.id.conversationID;
import static com.markit.android.R.id.message_text;

public class MessageDetail extends BaseActivity implements FirebaseAuth.AuthStateListener {

    public static final String TAG = "Chat";
    private FirebaseAuth firebaseAuth;
    private Button sendButton;
    private EditText editMessage;
    private Button backButton;

    private FirebaseRecyclerAdapter<Chat, MessageViewHolder> recViewAdapter;

    public Context context = this;
    private String conversationID;
    private String otherUser;
    private String otherUsername;

    private RecyclerView messageList;
    private LinearLayoutManager llm;


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference convoRefPush = database.getReference().child("users/" + getUID() + "/chats/");

    DatabaseReference sellerRefPush;
    //DatabaseReference chatRef = contextRef.child(ItemDetail.conversationKey + "/context");
   // DatabaseReference usernameRef = database.getReference().child("users/" + getUID() + "/username");

    //this pushes it to the other person correctly
    //DatabaseReference sellerRef = database.getReference().child("users/" + ItemDetail.otherUser + "/chats/" + NewConversationActivity.conversationKey + "/messages");
    DatabaseReference otherContextRef = database.getReference().child("users/" + ItemDetail.otherUser + "/chats/" + NewConversationActivity.conversationKey + "/context/" + "latestPost");

    DatabaseReference contextRef;
    //DatabaseReference otherContextRef;
    DatabaseReference sellerRef;

    DatabaseReference chatRefPush;
    //DatabaseReference chatRef = convoRefPush.child(conversationID + "/messages");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue_chat);
        Bundle idInfo = getIntent().getExtras();
        if (idInfo != null) {
            conversationID = idInfo.getString("conversationID");
            otherUser = idInfo.getString("otherUser");
            otherUsername = idInfo.getString("otherUsername");
        } else {
            conversationID = "-KX9d_FL3zJVZgvnl8TW";
        }

        chatRefPush = convoRefPush.child(conversationID + "/messages");
        contextRef = convoRefPush.child(conversationID + "/context/" + "latestPost");

        sellerRef = database.getReference().child("users/" + ItemDetail.otherUser + "/chats/" + conversationID + "/messages");

        ///final DatabaseReference sellerRefPush =  database.getReference().child("users/" + ItemDetail.otherUser + "/chats/" + ItemDetail.conversationKey + "/messages");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(this);

        //This pushes it to the correct conversation in the Database
        DatabaseReference convoRef = database.getReference().child("users/" + getUID() + "/chats/");
        final DatabaseReference chatRef = convoRef.child(conversationID + "/messages");

        //this pushes it to the username - but its getting a null username

        final DatabaseReference otherUserRef = database.getReference().child("users/" + otherUsername + "/chats/" + conversationID + "/messages");

        //otherContextRef = database.getReference().child("users/" + otherUser + "/chats/" + conversationID + "/context/" + "latestPost");

        backButton = (Button) findViewById(R.id.backButton);

        sendButton = (Button) findViewById(R.id.sendButton);
        editMessage = (EditText) findViewById(R.id.messageEdit);

        messageList = (RecyclerView) findViewById(R.id.messagesList);


//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainChatActivity.this, ChatListView.class));
//            }
//        });

        llm = new LinearLayoutManager(this);
        llm.setReverseLayout(false);

        messageList.setHasFixedSize(false);
        messageList.setLayoutManager(llm);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fix to get username not uid
                String uid = firebaseAuth.getCurrentUser().getUid();
                String user = uid;
                String type = "text";
                Date date = new Date();
                SimpleDateFormat fmt = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z '('z')'");
                String newDate = fmt.format(date);

                //message item itself
                Chat message = new Chat(editMessage.getText().toString(), user, newDate, type);

                //This pushes it to the correct conversation in firebase
                //contextRef.push(setValue)

                chatRefPush.push().setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference reference) {
                        if (databaseError != null) {
                            Log.e(TAG, "Failed to write message", databaseError.toException());
                        }
                    }
                });

                contextRef.setValue(newDate);
                System.out.println(newDate);


                //This pushes it to the otherUser's chats
                sellerRef.push().setValue(message, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference reference) {
                        if (databaseError != null) {
                            Log.e(TAG, "Failed to write message", databaseError.toException());
                        }
                    }
                });

                otherContextRef.setValue(newDate);

                editMessage.setText("");
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        attachRecyclerViewAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recViewAdapter != null) {
            recViewAdapter.cleanup();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(this);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        updateUI();
    }

    private void attachRecyclerViewAdapter() {
        Query lastFifty = chatRefPush.limitToLast(50);
        recViewAdapter = new FirebaseRecyclerAdapter<Chat, MessageViewHolder>(
                Chat.class, R.layout.chat_message, MessageViewHolder.class, lastFifty) {

            @Override
            public void populateViewHolder(MessageViewHolder chatView, Chat chat, int position) {
                //This displays the username as well but we don't really need it
                //chatView.setUser(chat.getUser());
                chatView.setText(chat.getText());

                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null && chat.getUser().equals(currentUser.getUid())) {
                    chatView.setIsSender(true);
                } else {
                    chatView.setIsSender(false);
                }
            }
        };

        // Scroll to bottom on new messages
        recViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                llm.smoothScrollToPosition(messageList, null, recViewAdapter.getItemCount());
            }
        });

        messageList.setAdapter(recViewAdapter);
    }


    public boolean isSignedIn() {
        return (firebaseAuth.getCurrentUser() != null);
    }

    public void updateUI() {
        // Sending only allowed when signed in
        sendButton.setEnabled(isSignedIn());
        editMessage.setEnabled(isSignedIn());
    }

    public static class Chat {

        String user;
        String text;
        String uid;
        String date;
        String type;

        public Chat() {
        }

        public Chat(String message) {
            this.text = text;
        }

        Chat(String text, String sender, String date, String type) {
            this.text = text;
            this.date = date;
            this.user = sender;
            this.type = type;

        }

        public String getUser() {
            return user;
        }

        public String getUid() {
            return uid;
        }

        public String getText() {
            return text;
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        Context context;
        TextView messageTime;
        TextView user;
        private final LinearLayout message;
        private final RelativeLayout messageContainer;
        private final FrameLayout left_arrow;
        private final FrameLayout right_arrow;

        public MessageViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            messageTime = (TextView) itemView.findViewById(R.id.message_time);
            messageText = (TextView) itemView.findViewById(R.id.message_text);
            user = (TextView) itemView.findViewById(R.id.user);
            left_arrow = (FrameLayout) itemView.findViewById(R.id.left_arrow);
            right_arrow = (FrameLayout) itemView.findViewById(R.id.right_arrow);
            messageContainer = (RelativeLayout) itemView.findViewById(R.id.message_container);
            message = (LinearLayout) itemView.findViewById(R.id.lmessage);
        }

        public void setIsSender(Boolean isSender) {
            int color;
            if (isSender) {
                color = ContextCompat.getColor(itemView.getContext(), R.color.wallet_holo_blue_light);
                left_arrow.setVisibility(View.GONE);
                right_arrow.setVisibility(View.VISIBLE);
                messageContainer.setGravity(Gravity.END);
            } else {
                color = ContextCompat.getColor(itemView.getContext(), R.color.wallet_secondary_text_holo_dark);
                left_arrow.setVisibility(View.VISIBLE);
                right_arrow.setVisibility(View.GONE);
                messageContainer.setGravity(Gravity.START);
            }

            message.setBackgroundColor(color);
        }

        public void setUser(String user) {
            TextView field = (TextView) itemView.findViewById(R.id.user);
            field.setText(user);
        }

        public void setText(String text) {
            TextView field = (TextView) itemView.findViewById(message_text);
            field.setText(text);
        }

    }
}