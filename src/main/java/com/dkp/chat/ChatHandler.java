//package com.animalmatch.chat;
//
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import org.apache.tomcat.util.json.JSONParser;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.BinaryMessage;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import lombok.extern.log4j.Log4j2;
//
//@Component
//@Log4j2
//public class ChatHandler extends TextWebSocketHandler {
//
////    private static List<WebSocketSession> list = new ArrayList<>();
//    List<HashMap<String, Object>> list = new ArrayList<>(); //웹소켓 세션을 담아둘 리스트 ---roomListSessions
//    private static final String FILE_UPLOAD_PATH = "C:/test/websocket/";
//	static int fileUploadIdx = 0;
//	static String fileUploadSession = "";
//    
////    @Override
////    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
////        String payload = message.getPayload();
////        log.info("payload : " + payload);
////
////        for(WebSocketSession sess: list) {
////            sess.sendMessage(message);
////        }
////    }
//
////    /* Client가 접속 시 호출되는 메서드 */
////    @Override
////    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
////
////        list.add(session);
////
////        log.info(session + " 클라이언트 접속");
////    }
//
//    /* Client가 접속 해제 시 호출되는 메서드드 */
//
////    @Override
////    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
////
////        log.info(session + " 클라이언트 접속 해제");
////        list.remove(session);
////        
////    }
//    
//}