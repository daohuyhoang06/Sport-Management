package com.sportmanagement.manager.ui.state

import com.sportmanagement.manager.domain.model.ChatMessage
import com.sportmanagement.manager.domain.model.ConversationItem
import com.sportmanagement.manager.domain.model.ReviewItem

data class MessagesUiState(
    val searchQuery: String = "",
    val conversations: List<ConversationItem> = demoConversations(),
    val reviews: List<ReviewItem> = demoReviews(),
    val selectedConversation: ConversationItem? = null,
    val chatMessages: Map<String, List<ChatMessage>> = demoChatMessages(),
    val draftMessage: String = "",
    val averageRating: Float = 4.8f,
    val totalReviews: Int = 128,
    val replyDrafts: Map<String, String> = emptyMap()
) {
    val filteredConversations: List<ConversationItem>
        get() = if (searchQuery.isBlank()) conversations
        else conversations.filter {
            it.customerName.contains(searchQuery, ignoreCase = true) ||
                it.customerPhone.contains(searchQuery)
        }

    val unreadCount: Int get() = conversations.sumOf { it.unreadCount }
}

fun demoConversations() = listOf(
    ConversationItem(
        id = "conv1",
        customerName = "Nguyễn Minh Tuấn",
        customerPhone = "090 123 4567",
        customerAvatarUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuA6A7IY2gN7K2Pnx9pxyH8SiM6oE5n3p5K0XQtWc9R4sZQ5xj2x4mMCCrVGxQL3n5oUAVmBFZf9zqfQKs0pDHX2k2xG1nM0zKjzI2P58s9tPqT",
        isOnline = true,
        lastMessage = "Chào anh, sân số 5 tối nay còn không?",
        lastMessageTime = "10:45",
        unreadCount = 2,
        totalBookings = 12
    ),
    ConversationItem(
        id = "conv2",
        customerName = "Lê Thị Hoa",
        customerPhone = "091 888 7777",
        customerAvatarUrl = null,
        isOnline = false,
        lastMessage = "Cảm ơn sân rất nhiều, dịch vụ rất tốt!",
        lastMessageTime = "Hôm qua",
        unreadCount = 0,
        totalBookings = 5
    ),
    ConversationItem(
        id = "conv3",
        customerName = "Trần Hoàng Nam",
        customerPhone = "098 765 4321",
        customerAvatarUrl = null,
        isOnline = false,
        lastMessage = "Tôi muốn đặt lịch cố định vào thứ 7 hàng tuần",
        lastMessageTime = "08:12",
        unreadCount = 1,
        totalBookings = 8
    )
)

fun demoChatMessages(): Map<String, List<ChatMessage>> = mapOf(
    "conv1" to listOf(
        ChatMessage("m1", "Chào anh, sân số 5 tối nay còn không ạ?", false, "10:44"),
        ChatMessage("m2", "Dạ sân A1 hiện còn trống từ 17h-19h anh ơi.", true, "10:45"),
        ChatMessage("m3", "Chào anh, sân số 5 tối nay còn không?", false, "10:45"),
        ChatMessage("m4", "Anh muốn đặt sân A1 từ 17h không? Em giữ chỗ cho anh nhé.", true, "10:46"),
        ChatMessage("m5", "Vâng anh đặt cho tôi từ 17h đến 18h30 nhé", false, "10:47"),
        ChatMessage("m6", "Dạ em xác nhận lịch đặt cho anh. Anh chú ý đặt cọc 100k trước 16h nhé.", true, "10:48")
    ),
    "conv2" to listOf(
        ChatMessage("m7", "Cảm ơn sân rất nhiều, dịch vụ rất tốt!", false, "Hôm qua 19:30"),
        ChatMessage("m8", "Cảm ơn chị đã tin tưởng sử dụng dịch vụ. Chúc chị và đội bóng thi đấu tốt!", true, "Hôm qua 19:35")
    ),
    "conv3" to listOf(
        ChatMessage("m9", "Tôi muốn đặt lịch cố định vào thứ 7 hàng tuần", false, "08:12")
    )
)

fun demoReviews() = listOf(
    ReviewItem(
        id = "r1",
        customerName = "Phạm Quốc Anh",
        customerAvatarUrl = null,
        rating = 5,
        content = "Sân đẹp, mặt cỏ rất êm. Tuy nhiên đèn sân hơi tối ở góc bên trái. Mong quản lý khắc phục.",
        timestamp = "23/10/2023",
        pitchName = "Sân 5 (A1)",
        courtName = "Sân 5 người A1"
    ),
    ReviewItem(
        id = "r2",
        customerName = "Đỗ Minh Khoa",
        customerAvatarUrl = null,
        rating = 4,
        content = "Nhân viên thân thiện, giá cả hợp lý. Bãi đỗ xe hơi chật một chút.",
        timestamp = "22/10/2023",
        pitchName = "Sân 7 (B1)",
        courtName = "Sân 7 người B1",
        managerReply = "Cảm ơn bạn đã góp ý. Chúng tôi sẽ cải thiện bãi đỗ xe sớm!",
        replyTimestamp = "22/10/2023"
    )
)
