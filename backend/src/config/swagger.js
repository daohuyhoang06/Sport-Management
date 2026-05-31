const jsonRequest = {
  required: true,
  content: {
    "application/json": {
      schema: {
        type: "object",
        additionalProperties: false,
        example: {},
      },
    },
  },
};

const schemaRequest = (schemaRef) => ({
  required: true,
  content: {
    "application/json": {
      schema: {
        $ref: `#/components/schemas/${schemaRef}`,
      },
    },
  },
});

const multipartRequest = {
  required: true,
  content: {
    "multipart/form-data": {
      schema: {
        type: "object",
        additionalProperties: false,
        example: {},
      },
    },
  },
};

const okResponse = {
  description: "Success",
  content: {
    "application/json": {
      schema: {
        $ref: "#/components/schemas/ApiResponse",
      },
    },
  },
};

const errorResponse = {
  description: "Error",
  content: {
    "application/json": {
      schema: {
        $ref: "#/components/schemas/ErrorResponse",
      },
    },
  },
};

const authSecurity = [{ bearerAuth: [] }];

const idParam = (name, description) => ({
  name,
  in: "path",
  required: true,
  schema: { type: "string" },
  description,
});

const swaggerSpec = {
  openapi: "3.0.0",
  info: {
    title: "Sport Management API",
    version: "1.0.0",
    description: "Complete API documentation for Sport Field Management System",
  },
  servers: [
    {
      url: "http://localhost:5000",
      description: "Local development server",
    },
  ],
  tags: [
    { name: "System", description: "Service and health endpoints" },
    { name: "Auth", description: "Authentication and password reset" },
    { name: "User", description: "Customer-facing endpoints" },
    { name: "Admin", description: "Admin-only endpoints" },
    { name: "Manager", description: "Manager-only endpoints" },
    { name: "Chat", description: "Chat endpoints" },
    { name: "AI", description: "AI assistant endpoints" },
    { name: "Alias", description: "Alias routes" },
  ],
  components: {
    securitySchemes: {
      bearerAuth: {
        type: "http",
        scheme: "bearer",
        bearerFormat: "JWT",
      },
    },
    schemas: {
      ApiResponse: {
        type: "object",
        properties: {
          success: { type: "boolean", example: true },
          message: { type: "string", example: "Operation successful" },
          data: { type: "object", additionalProperties: true },
        },
      },
      ErrorResponse: {
        type: "object",
        properties: {
          success: { type: "boolean", example: false },
          message: { type: "string", example: "Something went wrong" },
          error: { type: "string", example: "ValidationError" },
        },
      },
      RegisterRequest: {
        type: "object",
        required: ["name", "username", "password"],
        properties: {
          name: { type: "string", example: "" },
          username: { type: "string", example: "" },
          password: { type: "string", example: "" },
          email: {
            type: "string",
            format: "email",
            example: "",
          },
          phone: { type: "string", example: "" },
          birthday: { type: "string", format: "date", example: "" },
          sex: { type: "string", example: "" },
          address: { type: "string", example: "" },
        },
      },
      LoginRequest: {
        type: "object",
        required: ["username", "password"],
        properties: {
          username: {
            type: "string",
            description: "Username",
            example: "",
          },
          password: { type: "string", example: "" },
        },
      },
      RefreshTokenRequest: {
        type: "object",
        required: ["refreshToken"],
        properties: {
          refreshToken: { type: "string", example: "" },
        },
      },
      ForgotPasswordRequest: {
        type: "object",
        required: ["email"],
        properties: {
          email: { type: "string", format: "email", example: "" },
        },
      },
      VerifyOtpRequest: {
        type: "object",
        required: ["email", "otp"],
        properties: {
          email: { type: "string", format: "email", example: "" },
          otp: { type: "string", example: "" },
        },
      },
      ResetPasswordRequest: {
        type: "object",
        required: ["email", "otp", "newPassword"],
        properties: {
          email: { type: "string", format: "email", example: "" },
          otp: { type: "string", example: "" },
          newPassword: { type: "string", example: "" },
        },
      },
      CreateBookingRequest: {
        type: "object",
        required: ["field_id", "start_time", "end_time", "price"],
        properties: {
          field_id: { type: "integer", example: 0 },
          start_time: {
            type: "string",
            format: "date-time",
            example: "",
          },
          end_time: {
            type: "string",
            format: "date-time",
            example: "",
          },
          price: { type: "number", example: 0 },
          note: { type: "string", example: "" },
          customer_name: { type: "string", example: "" },
          customer_phone: { type: "string", example: "" },
        },
      },
      UpdateBookingRequest: {
        type: "object",
        properties: {
          payment_method: { type: "string", example: "" },
          status: { type: "string", example: "" },
          reason: { type: "string", example: "" },
        },
      },
      CreateReviewRequest: {
        type: "object",
        required: ["field_id", "rating"],
        properties: {
          field_id: { type: "integer", example: 0 },
          customer_id: { type: "integer", example: 0 },
          rating: { type: "integer", minimum: 1, maximum: 5, example: 1 },
          comment: { type: "string", example: "" },
          images: {
            type: "array",
            items: { type: "string" },
            example: [],
          },
        },
      },
      BookingStatusUpdateRequest: {
        type: "object",
        required: ["status"],
        properties: {
          status: { type: "string", example: "" },
          note: { type: "string", example: "" },
        },
      },
      CancelBookingRequest: {
        type: "object",
        properties: {
          reason: { type: "string", example: "" },
        },
      },
      AssignFieldRequest: {
        type: "object",
        required: ["employeeId", "fieldId"],
        properties: {
          employeeId: { type: "integer", example: 0 },
          fieldId: { type: "integer", example: 0 },
        },
      },
      ManagerCreateFieldRequest: {
        type: "object",
        required: ["field_name", "location", "slot_price"],
        properties: {
          field_name: { type: "string", example: "" },
          location: { type: "string", example: "" },
          sport_id: { type: "integer", example: 1 },
          latitude: { type: "number", example: 21.028511 },
          longitude: { type: "number", example: 105.804817 },
          phone: { type: "string", example: "0901234567" },
          open_time: { type: "string", example: "06:00:00" },
          close_time: { type: "string", example: "23:00:00" },
          slot_price: { type: "number", example: 0 },
          slot_minutes: { type: "integer", example: 60 },
          avatar_image_url: { type: "string", example: "" },
          card_image_url: { type: "string", example: "" },
          status: { type: "string", example: "active" },
        },
      },
      ManagerUpdateFieldRequest: {
        type: "object",
        properties: {
          field_name: { type: "string", example: "" },
          location: { type: "string", example: "" },
          sport_id: { type: "integer", example: 1 },
          latitude: { type: "number", example: 21.028511 },
          longitude: { type: "number", example: 105.804817 },
          phone: { type: "string", example: "0901234567" },
          open_time: { type: "string", example: "06:00:00" },
          close_time: { type: "string", example: "23:00:00" },
          slot_price: { type: "number", example: 0 },
          slot_minutes: { type: "integer", example: 60 },
          avatar_image_url: { type: "string", example: "" },
          card_image_url: { type: "string", example: "" },
          status: { type: "string", example: "active" },
        },
      },
      ManagerFieldStatusRequest: {
        type: "object",
        required: ["status"],
        properties: {
          status: { type: "string", example: "" },
        },
      },
      ManagerFieldCourtRequest: {
        type: "object",
        required: ["court_code", "court_name"],
        properties: {
          court_code: { type: "string", example: "SAN-A-01" },
          court_name: { type: "string", example: "San A1" },
          status: { type: "string", example: "active" },
          sort_order: { type: "integer", example: 1 },
        },
      },
      ManagerFieldCourtReorderRequest: {
        type: "object",
        required: ["courts"],
        properties: {
          courts: {
            type: "array",
            items: {
              type: "object",
              required: ["court_id", "sort_order"],
              properties: {
                court_id: { type: "integer", example: 1 },
                sort_order: { type: "integer", example: 1 },
              },
            },
          },
        },
      },
      ManagerFieldServiceRequest: {
        type: "object",
        required: ["service_name"],
        properties: {
          service_name: { type: "string", example: "Nuoc uong" },
          description: { type: "string", example: "Ban nuoc tai san" },
          is_free: { type: "boolean", example: false },
          price: { type: "number", example: 15000 },
        },
      },
      ManagerFieldPolicyRequest: {
        type: "object",
        required: ["title", "content", "policy_type"],
        properties: {
          title: { type: "string", example: "Khong hut thuoc" },
          content: {
            type: "string",
            example: "Khong hut thuoc trong khuon vien san",
          },
          policy_type: { type: "string", example: "general" },
        },
      },
      CreateChatRequest: {
        type: "object",
        required: ["managerId"],
        properties: {
          managerId: { type: "integer", example: 0 },
        },
      },
      SendMessageRequest: {
        type: "object",
        required: ["message"],
        properties: {
          message: { type: "string", example: "" },
        },
      },
      AiChatRequest: {
        type: "object",
        required: ["message"],
        properties: {
          message: { type: "string", example: "" },
          conversationHistory: {
            type: "array",
            items: { type: "object", additionalProperties: true },
            example: [],
          },
        },
      },
      RecommendFieldsRequest: {
        type: "object",
        properties: {
          location: { type: "string", example: "" },
          budget: { type: "number", example: 0 },
          time: { type: "string", example: "" },
          playerCount: { type: "integer", example: 0 },
        },
      },
      DetectFraudRequest: {
        type: "object",
        required: ["bookingDetails"],
        properties: {
          bookingDetails: { type: "object", additionalProperties: true },
        },
      },
    },
  },
  paths: {
    "/": {
      get: {
        tags: ["System"],
        summary: "Get service metadata",
        responses: { 200: okResponse },
      },
    },
    "/health": {
      get: {
        tags: ["System"],
        summary: "Get app health status",
        responses: { 200: okResponse },
      },
    },
    "/api/health": {
      get: {
        tags: ["System"],
        summary: "Get API and DB health status",
        responses: { 200: okResponse, 500: errorResponse },
      },
    },
    "/api/docs-json": {
      get: {
        tags: ["System"],
        summary: "Get OpenAPI JSON document",
        responses: { 200: okResponse },
      },
    },

    "/api/auth/register": {
      post: {
        tags: ["Auth"],
        summary: "Register a new account",
        requestBody: schemaRequest("RegisterRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/auth/login": {
      post: {
        tags: ["Auth"],
        summary: "Login",
        requestBody: schemaRequest("LoginRequest"),
        responses: { 200: okResponse, 401: errorResponse },
      },
    },
    "/api/auth/refresh": {
      post: {
        tags: ["Auth"],
        summary: "Refresh access token",
        requestBody: schemaRequest("RefreshTokenRequest"),
        responses: { 200: okResponse, 401: errorResponse },
      },
    },
    "/api/auth/forgot-password": {
      post: {
        tags: ["Auth"],
        summary: "Request password reset OTP",
        requestBody: schemaRequest("ForgotPasswordRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/auth/verify-otp": {
      post: {
        tags: ["Auth"],
        summary: "Verify password reset OTP",
        requestBody: schemaRequest("VerifyOtpRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/auth/reset-password": {
      post: {
        tags: ["Auth"],
        summary: "Reset password",
        requestBody: schemaRequest("ResetPasswordRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/auth/resend-otp": {
      post: {
        tags: ["Auth"],
        summary: "Resend password reset OTP",
        requestBody: schemaRequest("ForgotPasswordRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/auth/me": {
      get: {
        tags: ["Auth"],
        summary: "Get current user",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse },
      },
    },
    "/api/auth/logout": {
      post: {
        tags: ["Auth"],
        summary: "Logout",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse },
      },
    },

    "/api/user/ping": {
      get: {
        tags: ["User"],
        summary: "Ping user route",
        responses: { 200: okResponse },
      },
    },
    "/api/user/fields": {
      get: {
        tags: ["User"],
        summary: "List fields",
        responses: { 200: okResponse },
      },
    },
    "/api/user/fields/{id}": {
      get: {
        tags: ["User"],
        summary: "Get field detail",
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse, 404: errorResponse },
      },
    },
    "/api/user/fields/{id}/bookings": {
      get: {
        tags: ["User"],
        summary: "Get bookings for a field",
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse },
      },
    },
    "/api/user/bookings/history": {
      get: {
        tags: ["User"],
        summary: "Get current user booking history",
        responses: { 200: okResponse },
      },
    },
    "/api/user/bookings": {
      post: {
        tags: ["User"],
        summary: "Create booking",
        security: authSecurity,
        requestBody: schemaRequest("CreateBookingRequest"),
        responses: { 201: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
    "/api/user/bookings/{id}": {
      get: {
        tags: ["User"],
        summary: "Get booking detail",
        parameters: [idParam("id", "Booking ID")],
        responses: { 200: okResponse, 404: errorResponse },
      },
      put: {
        tags: ["User"],
        summary: "Update booking",
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("UpdateBookingRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/user/inbox": {
      get: {
        tags: ["User"],
        summary: "Get inbox sections",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse },
      },
    },
    "/api/user/reviews/upload": {
      post: {
        tags: ["User"],
        summary: "Upload review images",
        security: authSecurity,
        requestBody: multipartRequest,
        responses: { 200: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
    "/api/user/reviews": {
      get: {
        tags: ["User"],
        summary: "Get reviews",
        responses: { 200: okResponse },
      },
      post: {
        tags: ["User"],
        summary: "Create review",
        security: authSecurity,
        requestBody: schemaRequest("CreateReviewRequest"),
        responses: { 201: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
    "/api/user/reviews/stats/{fieldId}": {
      get: {
        tags: ["User"],
        summary: "Get review stats by field",
        parameters: [idParam("fieldId", "Field ID")],
        responses: { 200: okResponse },
      },
    },

    "/api/users/ping": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/ping",
        responses: { 200: okResponse },
      },
    },
    "/api/users/fields": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/fields",
        responses: { 200: okResponse },
      },
    },
    "/api/users/fields/{id}": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/fields/{id}",
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse, 404: errorResponse },
      },
    },
    "/api/users/fields/{id}/bookings": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/fields/{id}/bookings",
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse },
      },
    },
    "/api/users/bookings/history": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/bookings/history",
        responses: { 200: okResponse },
      },
    },
    "/api/users/bookings": {
      post: {
        tags: ["Alias"],
        summary: "Alias for /api/user/bookings",
        security: authSecurity,
        requestBody: schemaRequest("CreateBookingRequest"),
        responses: { 201: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
    "/api/users/bookings/{id}": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/bookings/{id}",
        parameters: [idParam("id", "Booking ID")],
        responses: { 200: okResponse, 404: errorResponse },
      },
      put: {
        tags: ["Alias"],
        summary: "Alias for /api/user/bookings/{id}",
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("UpdateBookingRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/users/reviews/upload": {
      post: {
        tags: ["Alias"],
        summary: "Alias for /api/user/reviews/upload",
        security: authSecurity,
        requestBody: multipartRequest,
        responses: { 200: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
    "/api/users/reviews": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/reviews",
        responses: { 200: okResponse },
      },
      post: {
        tags: ["Alias"],
        summary: "Alias for /api/user/reviews",
        security: authSecurity,
        requestBody: schemaRequest("CreateReviewRequest"),
        responses: { 201: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
    "/api/users/reviews/stats/{fieldId}": {
      get: {
        tags: ["Alias"],
        summary: "Alias for /api/user/reviews/stats/{fieldId}",
        parameters: [idParam("fieldId", "Field ID")],
        responses: { 200: okResponse },
      },
    },

    "/api/admin/dashboard": {
      get: {
        tags: ["Admin"],
        summary: "Get admin dashboard",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/revenue/date-range": {
      get: {
        tags: ["Admin"],
        summary: "Get revenue by date range",
        security: authSecurity,
        parameters: [
          {
            name: "from",
            in: "query",
            schema: { type: "string" },
            description: "Start date",
          },
          {
            name: "to",
            in: "query",
            schema: { type: "string" },
            description: "End date",
          },
        ],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/revenue/field/{fieldId}": {
      get: {
        tags: ["Admin"],
        summary: "Get revenue for a field",
        security: authSecurity,
        parameters: [idParam("fieldId", "Field ID")],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/revenue/monthly": {
      get: {
        tags: ["Admin"],
        summary: "Get monthly revenue statistics",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/users": {
      get: {
        tags: ["Admin"],
        summary: "List users",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
      post: {
        tags: ["Admin"],
        summary: "Create user",
        security: authSecurity,
        requestBody: jsonRequest,
        responses: {
          201: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/users/stats": {
      get: {
        tags: ["Admin"],
        summary: "Get user statistics",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/users/{id}": {
      get: {
        tags: ["Admin"],
        summary: "Get user by ID",
        security: authSecurity,
        parameters: [idParam("id", "User ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      put: {
        tags: ["Admin"],
        summary: "Update user",
        security: authSecurity,
        parameters: [idParam("id", "User ID")],
        requestBody: jsonRequest,
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      delete: {
        tags: ["Admin"],
        summary: "Delete user",
        security: authSecurity,
        parameters: [idParam("id", "User ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/users/{id}/status": {
      patch: {
        tags: ["Admin"],
        summary: "Toggle user status",
        security: authSecurity,
        parameters: [idParam("id", "User ID")],
        requestBody: jsonRequest,
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/fields": {
      get: {
        tags: ["Admin"],
        summary: "List fields",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
      post: {
        tags: ["Admin"],
        summary: "Create field",
        security: authSecurity,
        requestBody: jsonRequest,
        responses: {
          201: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/fields/stats": {
      get: {
        tags: ["Admin"],
        summary: "Get field statistics",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/fields/{id}": {
      get: {
        tags: ["Admin"],
        summary: "Get field by ID",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      put: {
        tags: ["Admin"],
        summary: "Update field",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: jsonRequest,
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      delete: {
        tags: ["Admin"],
        summary: "Delete field",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/fields/{id}/status": {
      patch: {
        tags: ["Admin"],
        summary: "Toggle field status",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: jsonRequest,
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/fields/{id}/images": {
      post: {
        tags: ["Admin"],
        summary: "Upload images for a field",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: multipartRequest,
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/fields/images/{imageId}": {
      delete: {
        tags: ["Admin"],
        summary: "Delete field image",
        security: authSecurity,
        parameters: [idParam("imageId", "Image ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/bookings": {
      get: {
        tags: ["Admin"],
        summary: "List bookings",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/bookings/stats": {
      get: {
        tags: ["Admin"],
        summary: "Get booking statistics",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/bookings/date-range": {
      get: {
        tags: ["Admin"],
        summary: "Get bookings by date range",
        security: authSecurity,
        parameters: [
          {
            name: "from",
            in: "query",
            schema: { type: "string" },
            description: "Start date",
          },
          {
            name: "to",
            in: "query",
            schema: { type: "string" },
            description: "End date",
          },
        ],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/bookings/{id}": {
      get: {
        tags: ["Admin"],
        summary: "Get booking by ID",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/bookings/{id}/status": {
      patch: {
        tags: ["Admin"],
        summary: "Update booking status",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("BookingStatusUpdateRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/bookings/{id}/cancel": {
      post: {
        tags: ["Admin"],
        summary: "Cancel booking",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("CancelBookingRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/employees": {
      get: {
        tags: ["Admin"],
        summary: "List employees",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
      post: {
        tags: ["Admin"],
        summary: "Create employee",
        security: authSecurity,
        requestBody: jsonRequest,
        responses: {
          201: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/employees/stats": {
      get: {
        tags: ["Admin"],
        summary: "Get employee statistics",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/admin/employees/{id}": {
      get: {
        tags: ["Admin"],
        summary: "Get employee by ID",
        security: authSecurity,
        parameters: [idParam("id", "Employee ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      put: {
        tags: ["Admin"],
        summary: "Update employee",
        security: authSecurity,
        parameters: [idParam("id", "Employee ID")],
        requestBody: jsonRequest,
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      delete: {
        tags: ["Admin"],
        summary: "Delete employee",
        security: authSecurity,
        parameters: [idParam("id", "Employee ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/admin/employees/assign-field": {
      post: {
        tags: ["Admin"],
        summary: "Assign field to employee",
        security: authSecurity,
        requestBody: schemaRequest("AssignFieldRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },

    "/api/manager/dashboard/stats": {
      get: {
        tags: ["Manager"],
        summary: "Get manager dashboard stats",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/manager/dashboard/revenue": {
      get: {
        tags: ["Manager"],
        summary: "Get manager revenue by date range",
        security: authSecurity,
        parameters: [
          {
            name: "from",
            in: "query",
            schema: { type: "string" },
            description: "Start date",
          },
          {
            name: "to",
            in: "query",
            schema: { type: "string" },
            description: "End date",
          },
        ],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/manager/dashboard/monthly-revenue": {
      get: {
        tags: ["Manager"],
        summary: "Get manager monthly revenue",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/manager/bookings": {
      get: {
        tags: ["Manager"],
        summary: "List manager bookings",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/manager/bookings/{id}": {
      get: {
        tags: ["Manager"],
        summary: "Get manager booking by ID",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/bookings/{id}/approve": {
      put: {
        tags: ["Manager"],
        summary: "Approve booking",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("CancelBookingRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/bookings/{id}/reject": {
      put: {
        tags: ["Manager"],
        summary: "Reject booking",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("CancelBookingRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/bookings/{id}/complete": {
      put: {
        tags: ["Manager"],
        summary: "Complete booking",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("CancelBookingRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/bookings/{id}/cancel": {
      put: {
        tags: ["Manager"],
        summary: "Cancel booking",
        security: authSecurity,
        parameters: [idParam("id", "Booking ID")],
        requestBody: schemaRequest("CancelBookingRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields": {
      get: {
        tags: ["Manager"],
        summary: "List manager fields",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
      post: {
        tags: ["Manager"],
        summary: "Create field",
        security: authSecurity,
        requestBody: schemaRequest("ManagerCreateFieldRequest"),
        responses: {
          201: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}": {
      get: {
        tags: ["Manager"],
        summary: "Get field by ID",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      put: {
        tags: ["Manager"],
        summary: "Update field",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: schemaRequest("ManagerUpdateFieldRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      patch: {
        tags: ["Manager"],
        summary: "Patch field configuration",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: schemaRequest("ManagerUpdateFieldRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      delete: {
        tags: ["Manager"],
        summary: "Delete field",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/config": {
      get: {
        tags: ["Manager"],
        summary: "Get full field configuration",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/status": {
      put: {
        tags: ["Manager"],
        summary: "Update field status",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: schemaRequest("ManagerFieldStatusRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/stats": {
      get: {
        tags: ["Manager"],
        summary: "Get field statistics",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
    },
    "/api/manager/fields/{id}/courts": {
      get: {
        tags: ["Manager"],
        summary: "List field courts",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
      post: {
        tags: ["Manager"],
        summary: "Create field court",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: schemaRequest("ManagerFieldCourtRequest"),
        responses: {
          201: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/courts/reorder": {
      patch: {
        tags: ["Manager"],
        summary: "Reorder field courts",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: schemaRequest("ManagerFieldCourtReorderRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/courts/{courtId}": {
      put: {
        tags: ["Manager"],
        summary: "Update field court",
        security: authSecurity,
        parameters: [idParam("id", "Field ID"), idParam("courtId", "Court ID")],
        requestBody: schemaRequest("ManagerFieldCourtRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      delete: {
        tags: ["Manager"],
        summary: "Delete field court",
        security: authSecurity,
        parameters: [idParam("id", "Field ID"), idParam("courtId", "Court ID")],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/services": {
      get: {
        tags: ["Manager"],
        summary: "List field services",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
      post: {
        tags: ["Manager"],
        summary: "Create field service",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: schemaRequest("ManagerFieldServiceRequest"),
        responses: {
          201: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/services/{serviceId}": {
      put: {
        tags: ["Manager"],
        summary: "Update field service",
        security: authSecurity,
        parameters: [
          idParam("id", "Field ID"),
          idParam("serviceId", "Service ID"),
        ],
        requestBody: schemaRequest("ManagerFieldServiceRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      delete: {
        tags: ["Manager"],
        summary: "Delete field service",
        security: authSecurity,
        parameters: [
          idParam("id", "Field ID"),
          idParam("serviceId", "Service ID"),
        ],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/policies": {
      get: {
        tags: ["Manager"],
        summary: "List field policies",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        responses: { 200: okResponse, 401: errorResponse, 403: errorResponse },
      },
      post: {
        tags: ["Manager"],
        summary: "Create field policy",
        security: authSecurity,
        parameters: [idParam("id", "Field ID")],
        requestBody: schemaRequest("ManagerFieldPolicyRequest"),
        responses: {
          201: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },
    "/api/manager/fields/{id}/policies/{policyId}": {
      put: {
        tags: ["Manager"],
        summary: "Update field policy",
        security: authSecurity,
        parameters: [
          idParam("id", "Field ID"),
          idParam("policyId", "Policy ID"),
        ],
        requestBody: schemaRequest("ManagerFieldPolicyRequest"),
        responses: {
          200: okResponse,
          400: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
      delete: {
        tags: ["Manager"],
        summary: "Delete field policy",
        security: authSecurity,
        parameters: [
          idParam("id", "Field ID"),
          idParam("policyId", "Policy ID"),
        ],
        responses: {
          200: okResponse,
          404: errorResponse,
          401: errorResponse,
          403: errorResponse,
        },
      },
    },

    "/api/chat/managers": {
      get: {
        tags: ["Chat"],
        summary: "Get available managers",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse },
      },
    },
    "/api/chat/list": {
      get: {
        tags: ["Chat"],
        summary: "Get user chats",
        security: authSecurity,
        responses: { 200: okResponse, 401: errorResponse },
      },
    },
    "/api/chat/create": {
      post: {
        tags: ["Chat"],
        summary: "Create or get chat",
        security: authSecurity,
        requestBody: schemaRequest("CreateChatRequest"),
        responses: { 200: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
    "/api/chat/{chatId}/messages": {
      get: {
        tags: ["Chat"],
        summary: "Get chat messages",
        security: authSecurity,
        parameters: [idParam("chatId", "Chat ID")],
        responses: { 200: okResponse, 401: errorResponse, 404: errorResponse },
      },
    },
    "/api/chat/{chatId}/send": {
      post: {
        tags: ["Chat"],
        summary: "Send message",
        security: authSecurity,
        parameters: [idParam("chatId", "Chat ID")],
        requestBody: schemaRequest("SendMessageRequest"),
        responses: { 200: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },

    "/api/ai/chat": {
      post: {
        tags: ["AI"],
        summary: "AI chat",
        requestBody: schemaRequest("AiChatRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/ai/recommend-fields": {
      post: {
        tags: ["AI"],
        summary: "Recommend fields",
        requestBody: schemaRequest("RecommendFieldsRequest"),
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/ai/weather": {
      get: {
        tags: ["AI"],
        summary: "Get weather info",
        parameters: [
          {
            name: "city",
            in: "query",
            schema: { type: "string" },
            description: "City name",
          },
          {
            name: "date",
            in: "query",
            schema: { type: "string" },
            description: "Date",
          },
        ],
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/ai/suggest-timeslots/{fieldId}": {
      get: {
        tags: ["AI"],
        summary: "Suggest time slots for a field",
        parameters: [idParam("fieldId", "Field ID")],
        responses: { 200: okResponse, 400: errorResponse },
      },
    },
    "/api/ai/detect-fraud": {
      post: {
        tags: ["AI"],
        summary: "Detect booking/payment fraud",
        security: authSecurity,
        requestBody: schemaRequest("DetectFraudRequest"),
        responses: { 200: okResponse, 400: errorResponse, 401: errorResponse },
      },
    },
  },
};

export default swaggerSpec;
