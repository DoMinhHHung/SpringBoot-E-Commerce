# Chat Widget Feature - Real-time Product Advice

This feature adds a real-time product advice chat widget to the product detail pages using WebSocket/STOMP protocol.

## Overview

The chat widget is a bot-only assistant that helps users find products based on their queries. It uses:
- **Backend**: Spring Boot WebSocket with STOMP messaging protocol
- **Frontend**: SockJS client for WebSocket connection and STOMP.js for messaging
- **Real-time**: Bidirectional communication between client and server

## Components Added

### Backend

1. **WebSocketConfig.java** (`src/main/java/iuh/fit/se/ecommerce/config/`)
   - Configures STOMP message broker
   - Registers `/ws` endpoint with SockJS support
   - Sets allowed origin patterns to `*` for development

2. **ChatMessage.java** (`src/main/java/iuh/fit/se/ecommerce/dto/`)
   - DTO for incoming chat messages
   - Fields: `sessionId`, `text`, `productId` (optional)

3. **ChatResponse.java** (`src/main/java/iuh/fit/se/ecommerce/dto/`)
   - DTO for outgoing responses
   - Contains: `text` message and list of `SimpleProduct` suggestions
   - SimpleProduct includes: `id`, `name`, `imageUrl`, `price`

4. **ChatWebSocketController.java** (`src/main/java/iuh/fit/se/ecommerce/controller/`)
   - Handles `@MessageMapping("/chat")` for incoming messages
   - Calls `ProductService.findByQuery()` to search products
   - Sends responses to `/topic/replies.{sessionId}`
   - Provides fallback messages when no products match

5. **ProductService & ProductServiceImpl** (updated)
   - Added `findByQuery(String query)` method
   - Searches by name or description (case-insensitive)
   - Returns up to 3 matching products

6. **ProductRepository** (updated)
   - Added `findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase()` method

7. **SecurityConfig** (updated)
   - Added `/ws/**` to permitted endpoints for WebSocket connections

### Frontend

1. **chat-widget.js** (`src/main/resources/static/js/`)
   - ChatWidget class with full lifecycle management
   - Connects to WebSocket endpoint `/ws` via SockJS
   - Subscribes to session-specific topic `/topic/replies.{sessionId}`
   - Sends messages to `/app/chat`
   - Handles UI rendering, message display, and product suggestions

2. **chat-widget.css** (`src/main/resources/static/css/`)
   - Modern, responsive design with gradient colors
   - Floating chat button in bottom-right corner
   - Chat window with header, messages area, and input field
   - Connection status indicator
   - Product suggestion cards with images and prices
   - Mobile-responsive breakpoints

3. **product-detail.html** (updated)
   - Added SockJS and STOMP.js library includes
   - Added chat-widget.css and chat-widget.js
   - Initializes widget with product ID when page loads

## How It Works

1. **User opens product detail page**: Widget button appears in bottom-right corner
2. **User clicks chat button**: Widget expands and connects to WebSocket server
3. **Connection established**: User can type messages
4. **User sends query** (e.g., "gaming" or "laptop"):
   - Message sent to `/app/chat` via STOMP
   - Backend searches products matching the query
   - Backend sends response to `/topic/replies.{sessionId}`
5. **Widget receives response**: Displays bot message with up to 3 product suggestions
6. **User clicks product**: Opens product detail in new tab

## Testing Instructions

### Prerequisites
- Spring Boot application running
- Database configured with product data
- Browser with JavaScript enabled

### Steps to Test

1. **Start the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Navigate to a product detail page**:
   - Open browser to `http://localhost:8080/product-detail.html?id=1`
   - (Replace `1` with an actual product ID from your database)

3. **Open the chat widget**:
   - Click the purple "Tư vấn" button in bottom-right corner
   - Wait for "Đã kết nối" (Connected) status

4. **Test queries**:
   - Try: "gaming" (should return gaming-related products)
   - Try: "laptop" (should return laptops)
   - Try: "dưới 25 triệu" (should return products with "25 triệu" in description)
   - Try: "xyz123abc" (should return "no matches" message)

5. **Verify results**:
   - Bot responds with relevant products (up to 3)
   - Each product shows: image, name, price, "Xem chi tiết" link
   - Clicking product link opens detail page in new tab

### Example Queries
- "gaming" - finds gaming products
- "laptop" - finds laptops
- "điện thoại" - finds phones
- "màn hình" - finds monitors
- Any Vietnamese or English product name/description keyword

## API Endpoints

### WebSocket
- **Endpoint**: `/ws`
- **Protocol**: SockJS + STOMP
- **Client Destination**: `/app/chat`
- **Server Destination**: `/topic/replies.{sessionId}`

### Message Format

**Client → Server** (ChatMessage):
```json
{
  "sessionId": "session-abc123-1699012345",
  "text": "gaming laptop",
  "productId": 5
}
```

**Server → Client** (ChatResponse):
```json
{
  "text": "Dưới đây là một số sản phẩm phù hợp với tìm kiếm của bạn:",
  "products": [
    {
      "id": 10,
      "name": "Gaming Laptop ASUS ROG",
      "imageUrl": "https://example.com/image.jpg",
      "price": 25000000
    }
  ]
}
```

## Configuration

### Allowed Origins
Currently set to `*` (all origins) for development. 

**For production**, update `WebSocketConfig.java`:
```java
registry.addEndpoint("/ws")
    .setAllowedOrigins("https://yourdomain.com")
    .withSockJS();
```

### Query Limit
The service returns up to 3 products per query. To change this, edit `ProductServiceImpl.java`:
```java
.limit(3)  // Change to desired number
```

## Browser Compatibility
- Chrome/Edge: ✅ Full support
- Firefox: ✅ Full support  
- Safari: ✅ Full support
- Mobile browsers: ✅ Responsive design

## Security Considerations

1. **No authentication required**: Widget works for all users (as per requirements)
2. **CSRF disabled**: WebSocket endpoints excluded from CSRF protection
3. **Input validation**: Text queries are escaped to prevent XSS
4. **Rate limiting**: Consider adding rate limiting in production
5. **Origin validation**: Update allowed origins for production deployment

## Future Enhancements

Potential improvements (not included in this PR):
- User authentication for personalized recommendations
- Chat history persistence
- Multi-language support
- Advanced NLP for better query understanding
- Price range filtering
- Product category suggestions
- Admin dashboard for monitoring chat interactions
- Integration with actual AI/ML recommendation engine

## Troubleshooting

### Widget not appearing
- Check browser console for JavaScript errors
- Verify chat-widget.js is loaded
- Check product-detail.html includes all required scripts

### Connection fails
- Verify WebSocket endpoint `/ws` is accessible
- Check SecurityConfig permits `/ws/**`
- Verify server is running

### No product results
- Check database has products with name/description matching query
- Verify ProductRepository method is working
- Check backend logs for errors

### Styling issues
- Verify chat-widget.css is loaded
- Check Bootstrap Icons CDN is accessible
- Test on different screen sizes
