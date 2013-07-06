/*
 * log.h
 *
 *  Created on: Jul 5, 2013
 *      Author: alessandro
 */

#ifndef LOG_H_
	#define LOG_H_


	#include <stdarg.h>
	#include <stdio.h>
	#include <stdlib.h>
	#include <android/log.h>

	#define LOG_TAG "exif-native"

	#ifdef LOG_ENABLED
		#define  LOGV(...)  __android_log_print( ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__ )
		#define  LOGI(...)  __android_log_print( ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__ )
		#define  LOGD(...)  __android_log_print( ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__ )
		#define  LOGW(...)  __android_log_print( ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__ )
	#else
		#define LOGV(...)
		#define LOGI(...)
		#define LOGD(...)
		#define LOGW(...)
	#endif // LOG_ENABLED

	// error log always enabled
	#define  LOGE(...)  __android_log_print( ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__ )


#endif /* LOG_H_ */
