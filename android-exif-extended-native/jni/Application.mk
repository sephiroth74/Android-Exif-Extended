
APP_PLATFORM	:= android-9
ENABLE_LOGGING	:= true


ifeq ($(HOST_OS),darwin)
	APP_ABI	:= x86 armeabi armeabi-v7a
else
	APP_ABI	:= x86
endif

APP_OPTIM	:= release
