sed -i 's/else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))/else -> dateFormat.format(Date(timestamp))/g' app/src/main/java/com/sepfol/app/ui/folder/FolderScreen.kt
