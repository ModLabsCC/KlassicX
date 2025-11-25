package cc.modlabs.klassicx.wrappers.transForge

/**
 * Data classes for Transforge API request and response models.
 * These match the schemas defined in the OpenAPI specification.
 */

// ==================== Health ====================

data class HealthResponse(
    val status: String
)

// ==================== Account 2FA ====================

data class BackupCodesCountResponse(
    val count: Int
)

data class RegenerateBackupCodesRequest(
    val totpCode: String
)

data class DisableTotpRequest(
    val code: String
)

data class EnableTotpRequest(
    val code: String
)

data class TotpSetupResponse(
    val secret: String,
    val qrCode: String
)

// ==================== Account ====================

data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

data class UpdateUsernameRequest(
    val username: String
)

// ==================== API ====================

data class MiniMessageRenderRequest(
    val text: String,
    val isolateNewlines: Boolean
)

data class MiniMessageRenderResponse(
    val html: String,
    val success: Boolean,
    val errorMessage: String? = null
)

// ==================== Suggestions ====================

data class ApproveSuggestionRequest(
    val value: String? = null
)

data class RejectSuggestionRequest(
    val notes: String? = null
)

data class SubmitSuggestionRequest(
    val key: String,
    val locale: String,
    val value: String
)

data class SuggestionResponse(
    val id: String,
    val translationId: String,
    val keyId: String,
    val key: String? = null,
    val locale: String,
    val suggestedValue: String,
    val status: String,
    val submittedByUserId: String? = null,
    val submittedAt: String,
    val reviewedByUserId: String? = null,
    val reviewedAt: String? = null,
    val reviewNotes: String? = null
)

// ==================== Translations ====================

data class CreateTranslationRequest(
    val name: String,
    val ownerTeamId: String? = null
)

data class TranslationResponse(
    val id: String,
    val name: String,
    val ownerUserId: String? = null,
    val ownerTeamId: String? = null,
    val createdAt: String
)

data class TransferOwnershipRequest(
    val newOwnerUserId: String? = null,
    val newOwnerTeamId: String? = null
)

data class GridDataResponse(
    val keys: List<KeyResponse>,
    val enabledLocales: List<LocaleResponse>,
    val values: List<ValueResponse>
)

data class ImportResponse(
    val success: Boolean,
    val keysCreated: Int,
    val valuesImported: Int,
    val errors: List<String>
)

data class CreateKeyRequest(
    val key: String
)

data class KeyResponse(
    val id: String,
    val key: String,
    val createdAt: String
)

data class LocaleResponse(
    val id: String,
    val translationId: String,
    val locale: String,
    val enabled: Boolean,
    val createdAt: String
)

data class SetLocaleRequest(
    val locale: String,
    val enabled: Boolean
)

data class PermissionResponse(
    val id: String,
    val translationId: String,
    val userId: String? = null,
    val teamId: String? = null,
    val role: String,
    val grantedByUserId: String? = null,
    val grantedAt: String
)

data class GrantPermissionRequest(
    val userId: String? = null,
    val teamId: String? = null,
    val role: String
)

data class UpdatePermissionRequest(
    val role: String
)

data class SetValueRequest(
    val keyId: String,
    val locale: String,
    val value: String? = null
)

data class ValueResponse(
    val id: String,
    val translationId: String,
    val keyId: String,
    val locale: String,
    val value: String? = null,
    val createdAt: String,
    val updatedAt: String
)

// ==================== Auth ====================

data class LoginRequest(
    val email: String,
    val password: String,
    val totpCode: String? = null
)

data class LoginResponse(
    val user: UserResponse? = null,
    val requires2FA: Boolean,
    val message: String? = null
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val totpEnabled: Boolean
)

// ==================== Teams ====================

data class CreateTeamRequest(
    val name: String
)

data class TeamResponse(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val createdAt: String
)

data class AddTeamMemberRequest(
    val email: String,
    val role: String? = null
)

data class TeamMemberResponse(
    val id: String,
    val teamId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val role: String,
    val joinedAt: String
)

