project("cvtt")
cmake_minimum_required(VERSION 3.22)
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-4.9.0-android/sdk/native/jni)
find_package(OpenCV REQUIRED)

add_library(native-lib SHARED ../cpp/native-lib.cpp)
target_link_libraries(native-lib ${OpenCV_LIBS})