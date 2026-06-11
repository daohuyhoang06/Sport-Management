const fs = require('fs');

function replaceOnce(text, before, after) {
  const idx = text.indexOf(before);
  if (idx === -1) throw new Error(`Missing block: ${before.slice(0, 40)}`);
  return text.slice(0, idx) + after + text.slice(idx + before.length);
}

const uiPath = 'frontend/mobile-user/app/src/main/kotlin/com/sportmanagement/user/ui/screens/UserInboxScreens.kt';
let ui = fs.readFileSync(uiPath, 'utf8');

const requesterCard = [
  '@Composable',
  'private fun MatchSuccessRequesterCard(info: NotificationDetailInfo.MatchSuccessNotice) {',
  '    Card(',
  '        modifier = Modifier.fillMaxWidth(),',
  '        shape = RoundedCornerShape(AppCardCornerRadius),',
  '        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),',
  '        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)',
  '    ) {',
  '        Column(modifier = Modifier.padding(14.dp)) {',
  '            SectionHeader(',
  '                title = "Thông tin người đặt",',
  '                icon = Icons.Outlined.Person',
  '            )',
  '            Spacer(Modifier.height(10.dp))',
  '            BookingDetailRow(label = "Tên user", value = info.requesterAccountName.ifBlank { "Đang cập nhật" })',
  '            BookingDetailRow(label = "Tên đội", value = info.requesterTeamName.ifBlank { "Đang cập nhật" })',
  '            BookingDetailRow(label = "Trình độ", value = info.requesterLevelLabel.ifBlank { "Đang cập nhật" })',
  '        }',
  '    }',
  '}',
  '',
  '@Composable',
].join('\n');
ui = replaceOnce(ui, requesterCard, '@Composable\n');

const successTitleBlock = [
  'private fun NotificationDetailInfo.MatchSuccessNotice.toPeerConversationInfo(): ConversationInfo {',
  '    val conversationTitle = ownerUsername',
  '        .ifBlank { requesterAccountName }',
  '        .ifBlank { "Hội thoại" }',
  '',
  '    return ConversationInfo(',
  '        fieldName = conversationTitle,',
  '        statusLabel = "Đang hoạt động",',
  '        phoneNumber = "",',
  '        avatarRes = R.drawable.field_football,',
  '        fieldId = fieldId,',
  '        bookingId = bookingId,',
  '        peerUserId = requesterUserId',
  '    )',
  '}',
].join('\n');
ui = replaceOnce(
  ui,
  [
    'private fun NotificationDetailInfo.MatchSuccessNotice.toPeerConversationInfo(): ConversationInfo {',
    '    val conversationTitle = ownerUsername',
    '        .ifBlank { requesterAccountName }',
    '        .ifBlank { "Hội thoại" }',
    '',
    '    return ConversationInfo(',
    '        fieldName = conversationTitle,',
    '        statusLabel = "Đang hoạt động",',
    '        phoneNumber = "",',
    '        avatarRes = R.drawable.field_football,',
    '        fieldId = fieldId,',
    '        bookingId = bookingId,',
    '        peerUserId = requesterUserId',
    '    )',
    '}',
  ].join('\n'),
  successTitleBlock
);
fs.writeFileSync(uiPath, ui, 'utf8');

const vmPath = 'frontend/mobile-user/app/src/main/kotlin/com/sportmanagement/user/ui/viewmodel/InboxViewModel.kt';
let vm = fs.readFileSync(vmPath, 'utf8');
vm = replaceOnce(
  vm,
  '                    requesterTeamName = requesterTeamName,\n                    ownerUsername = booking?.matchPost?.ownerUsername ?: "",\n                    requesterAccountName = requesterAccountName,\n',
  '                    requesterTeamName = requesterTeamName,\n                    ownerUsername = booking?.matchPost?.ownerUsername ?: "",\n                    requesterAccountName = requesterAccountName,\n'
);
vm = replaceOnce(
  vm,
  '                    requesterTeamName = requesterTeamName,\n                    requesterAccountName = requesterAccountName,\n',
  '                    requesterTeamName = requesterTeamName,\n                    ownerUsername = booking?.matchPost?.ownerUsername ?: "",\n                    requesterAccountName = requesterAccountName,\n'
);
vm = replaceOnce(
  vm,
  '        val requesterTeamName = resolvedCounterpartTeamName\n        val requesterAccountName = request?.requesterUsername ?: ""\n',
  '        val requesterTeamName = resolvedCounterpartTeamName\n        val ownerUsername = booking?.matchPost?.ownerUsername ?: ""\n        val requesterAccountName = request?.requesterUsername ?: ""\n'
);
vm = replaceOnce(
  vm,
  '                    requesterTeamName = requesterTeamName,\n                    ownerUsername = booking?.matchPost?.ownerUsername ?: "",\n                    requesterAccountName = requesterAccountName,\n',
  '                    requesterTeamName = requesterTeamName,\n                    ownerUsername = ownerUsername,\n                    requesterAccountName = requesterAccountName,\n'
);
vm = replaceOnce(
  vm,
  '                    requesterTeamName = requesterTeamName,\n                    requesterAccountName = requesterAccountName,\n',
  '                    requesterTeamName = requesterTeamName,\n                    ownerUsername = ownerUsername,\n                    requesterAccountName = requesterAccountName,\n'
);
vm = replaceOnce(
  vm,
  '                    requesterTeamName = requesterTeamName,\n                    requesterAccountName = requesterAccountName,\n',
  '                    requesterTeamName = requesterTeamName,\n                    ownerUsername = ownerUsername,\n                    requesterAccountName = requesterAccountName,\n'
);
vm = replaceOnce(
  vm,
  '        val requesterTeamName = resolvedCounterpartTeamName\n        val ownerUsername = booking?.matchPost?.ownerUsername ?: ""\n        val requesterAccountName = request?.requesterUsername ?: ""\n',
  '        val requesterTeamName = resolvedCounterpartTeamName\n        val ownerUsername = booking?.matchPost?.ownerUsername ?: ""\n        val requesterAccountName = request?.requesterUsername ?: ""\n'
);
vm = replaceOnce(
  vm,
  '        val requesterTeamName = resolvedCounterpartTeamName\n        val requesterAccountName = request?.requesterUsername ?: ""\n',
  '        val requesterTeamName = resolvedCounterpartTeamName\n        val ownerUsername = booking?.matchPost?.ownerUsername ?: ""\n        val requesterAccountName = request?.requesterUsername ?: ""\n'
);
vm = replaceOnce(
  vm,
  '                    ownerUsername = booking?.matchPost?.ownerUsername ?: "",\n',
  '                    ownerUsername = ownerUsername,\n'
);
fs.writeFileSync(vmPath, vm, 'utf8');
