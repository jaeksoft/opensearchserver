CXX = g++
CCFLAGS = -Wall -g -fPIC -O3
LDFLAGS  = -lc -shared
INCLUDES =

SRCS = \
	NativeBitSet.cc \
	NativeIntBufferedArray.cc \
	NativeFloatBufferedArray.cc

OBJS = $(SRCS:.cc=.o)
MAIN = nativeoss

ifeq ($(OS),Windows_NT)
	CCFLAGS += -D WIN32
	LIBNAME = ${MAIN}.dll
	ifeq ($(PROCESSOR_ARCHITECTURE),AMD64)
		CCFLAGS += -D AMD64
		endif
	ifeq ($(PROCESSOR_ARCHITECTURE),x86)
		CCFLAGS += -D IA32
	endif
else
	UNAME_S := $(shell uname -s)
	ifeq ($(UNAME_S),Linux)
		CCFLAGS += -D LINUX
		INCLUDES += -I/etc/alternatives/java_sdk_openjdk/include \
			-I/etc/alternatives/java_sdk_openjdk/include/linux \
			-I/usr/include/boost
		LIBNAME = lib${MAIN}.so
		endif
	ifeq ($(UNAME_S),Darwin)
		CCFLAGS += -D OSX
		INCLUDES += -I/usr/local/include/boost \
			-I/System/Library/Frameworks/JavaVM.framework/Versions/A/Headers
		LIBNAME = lib${MAIN}.dylib
	endif
	UNAME_M := $(shell uname -m)
	ifeq ($(UNAME_M),x86_64)
		CCFLAGS += -D AMD64
		endif
	ifneq ($(filter %86,$(UNAME_M)),)
		CCFLAGS += -D IA32
		endif
	ifneq ($(filter arm%,$(UNAME_M)),)
		CCFLAGS += -D ARM
	endif
endif

.PHONY: clean

all: $(MAIN)

$(MAIN): $(OBJS)
	$(CXX) $(LDFLAGS) -o $(LIBNAME) $(OBJS)

.cc.o:
	$(CXX) $(CCFLAGS) $(INCLUDES) -c $< -o $@

clean:
	$(RM) *.o *~ $(MAIN)
