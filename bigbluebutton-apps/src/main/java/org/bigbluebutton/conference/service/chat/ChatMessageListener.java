package org.bigbluebutton.conference.service.chat;

import org.bigbluebutton.conference.service.messaging.MessagingConstants;
import org.bigbluebutton.conference.service.messaging.redis.MessageHandler;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.HashMap;

import org.bigbluebutton.core.api.IBigBlueButtonInGW;

public class ChatMessageListener implements MessageHandler{

	private IBigBlueButtonInGW bbbGW;
	
	public void setBigBlueButtonInGW(IBigBlueButtonInGW bbbGW) {
		this.bbbGW = bbbGW;
	}

	@Override
	public void handleMessage(String pattern, String channel, String message) {
		if (channel.equalsIgnoreCase(MessagingConstants.TO_CHAT_CHANNEL)) {

			JsonParser parser = new JsonParser();
			JsonObject obj = (JsonObject) parser.parse(message);
			JsonObject headerObject = (JsonObject) obj.get("header");
			JsonObject payloadObject = (JsonObject) obj.get("payload");
			JsonObject messageObject = (JsonObject) payloadObject.get("message");

			String eventName = headerObject.get("name").toString();
			eventName = eventName.replace("\"", "");

			if (eventName.equalsIgnoreCase("public_chat_message_event") ||
				//eventName.equalsIgnoreCase("send_public_chat_message") ||
				eventName.equalsIgnoreCase("private_chat_message_event") ||
				//eventName.equalsIgnoreCase("send_private_chat_message") ||
				eventName.equalsIgnoreCase("get_chat_history")){

				String meetingID = payloadObject.get("meeting_id").toString().replace("\"", "");
				String requesterID = payloadObject.get("requester_id").toString().replace("\"", "");

				//case getChatHistory
				if(eventName.equalsIgnoreCase("get_chat_history")) {
					String replyTo = meetingID + "/" + requesterID;
					bbbGW.getChatHistory(meetingID, requesterID, replyTo);
				}
				else {
					String chatType = messageObject.get("chatType").toString().replace("\"", "");
					String fromUserID = messageObject.get("fromUserID").toString().replace("\"", "");
					String fromUsername = messageObject.get("fromUsername").toString().replace("\"", "");
					String fromColor = messageObject.get("fromColor").toString().replace("\"", "");
					String fromTime = messageObject.get("fromTime").toString().replace("\"", "");
					String fromTimezoneOffset = messageObject.get("fromTimezoneOffset").toString().replace("\"", "");
					String fromLang = messageObject.get("fromLang").toString().replace("\"", ""); 
					String toUserID = messageObject.get("toUserID").toString().replace("\"", "");
					String toUsername = messageObject.get("toUsername").toString().replace("\"", "");
					String chatText = messageObject.get("message").toString().replace("\"", "");

					Map<String, String> map = new HashMap<String, String>();
					map.put(ChatKeyUtil.CHAT_TYPE, chatType); 
					map.put(ChatKeyUtil.FROM_USERID, fromUserID);
					map.put(ChatKeyUtil.FROM_USERNAME, fromUsername);
					map.put(ChatKeyUtil.FROM_COLOR, fromColor);
					map.put(ChatKeyUtil.FROM_TIME, fromTime);
					map.put(ChatKeyUtil.FROM_TZ_OFFSET, fromTimezoneOffset);
					map.put(ChatKeyUtil.FROM_LANG, fromLang);
					map.put(ChatKeyUtil.TO_USERID, toUserID);
					map.put(ChatKeyUtil.TO_USERNAME, toUsername);
					map.put(ChatKeyUtil.MESSAGE, chatText);

					//public message
					if(eventName.equalsIgnoreCase("public_chat_message_event") || eventName.equalsIgnoreCase("send_public_chat_message")) //put this string into a constants file
					{
						System.out.println("I'm in the case for a public chat message" );

						bbbGW.sendPublicMessage(meetingID, requesterID, map);
					}

					//private message
					else if(eventName.equalsIgnoreCase("private_chat_message_event") || eventName.equalsIgnoreCase("send_private_chat_message")) //put this string into a constants file
					{
						System.out.println("I'm in the case for a private chat message" );

						bbbGW.sendPrivateMessage(meetingID, requesterID, map); //TODO not tested yet
					}
				}
			}
		}
	}
}
