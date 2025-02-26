package com.project.thisvsthat.chat.redis.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RedisSubscriptionService {
    private final RedisMessageListenerContainer redisContainer;
    private final MessageListenerAdapter messageListener;

    // 현재 구독 중인 채널을 관리 (중복 구독 방지)
    private final Map<String, ChannelTopic> subscribedChannels = new ConcurrentHashMap<>();

    // 채팅방 구독
    public void subscribeToChatRoom(String postId) {
        String channelKey = "chatroom:" + postId;

        // 이미 구독 중이면 다시 추가하지 않음
        if (subscribedChannels.containsKey(channelKey)) {
            return;
        }

        ChannelTopic topic = new ChannelTopic(channelKey);
        redisContainer.addMessageListener(messageListener, topic);  // 동적으로 채널 구독
        subscribedChannels.put(channelKey, topic);

        System.out.println("✅ 채팅방 구독 완료: " + channelKey);
    }

    // 채팅방 구독 해제
    public void unsubscribeFromChatRoom(String postId) {
        String channelKey = "chatroom:" + postId;

        ChannelTopic topic = subscribedChannels.remove(channelKey);
        if (topic != null) {
            redisContainer.removeMessageListener(messageListener, topic);  // 동적으로 채널 구독 해제
            System.out.println("❌ 채팅방 구독 해제: " + channelKey);
        }
    }
}