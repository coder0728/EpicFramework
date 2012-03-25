####################################################################################################
##  Phony Targets
####################################################################################################

.PHONY: all
all: lint EpicBuilder.jar EpicDesktop.jar EpicIos.jar

dist: all
	@echo "$(GREEN)General$(NOCOLOR) - Copying build artifacts to dist/"
	@mkdir -p dist dist/resources
	@cp build/EpicDesktop.jar dist/
	@cp EpicBuilder/resources/* dist/resources	

.PHONY: clean
clean:
	rm -rf build


####################################################################################################
##  Functions
####################################################################################################

classpathify = $(subst $(eval) ,:,$(wildcard $1))
logger       = echo "$(GREEN)$1$(NOCOLOR) -$2"


####################################################################################################
##  Variables
####################################################################################################


java_src_files_builder := $(shell find EpicBuilder/src -name '*.java' -type file -follow)
java_src_files_null    := $(shell find EpicNullPlat/src -name '*.java' -type file -follow)
java_src_files_common  := $(shell find EpicCommon/src -name '*.java' -type file -follow)
java_src_files_desktop := $(shell find EpicDesktop/src -name '*.java' -type file -follow)
java_src_files_ios     := $(shell find EpicIos/src -name '*.java' -type file -follow)
java_src_files_applet  := $(shell find EpicDesktop/src -name '*.java' -type file -follow)

JAVAC_PROCESSOR        := -processor com.epic.framework.build.EpicAnnotationProcessor

CP_COMM := build/classes_common
CP_DESK := build/classes_desktop
CP_IOS  := build/classes_ios
CP_BLD  := build/classes_builder
CP_AND  := build/classes_android

GREEN := \033[32m
YELLOW := \033[38;5;226m
NOCOLOR                := \033[39;0m

####################################################################################################
##  Targets
####################################################################################################
dbg:
	echo $(call classpathify,build/EpicCommon.jar EpicDesktop/lib/*.jar)	

node_modules: package.json
	npm install
	@touch node_modules

.PHONY: lint
lint:
	@echo "$(GREEN)General$(NOCOLOR) - Validating Javascript"
	@jshint  bin/underscore-template  bin/epic-resources jslib/*

.PHONY: EpicBuilder
EpicBuilder: build/.make.EpicBuilder
build/.make.EpicBuilder: $(java_src_files_builder)
	@mkdir -p build/classes_builder
	@echo "$(GREEN)EpicBuilder$(NOCOLOR) - Compiling $(words $(java_src_files_builder)) source files ..."
	@javac -d build/classes_builder $(java_src_files_builder)
	@#@cd build/classes_builder && for j in ../../EpicBuilder/lib/*.jar; do jar xf $$j; done
	@#@rsync -a EpicBuilder/src/ build/classes_builder/
	@#@echo "$(GREEN)EpicBuilder$(NOCOLOR) - jar cf build/EpicBuilder.jar -C build/classes_builder ."
	@#jar cf build/EpicBuilder.jar -C build/classes_builder .
	@touch build/.make.EpicBuilder

.PHONY: EpicCommon
EpicCommon: EpicBuilder build/.make.EpicCommon
build/.make.EpicCommon: $(java_src_files_common) $(java_src_files_null)
	@mkdir -p build/classes_common
	@echo "$(GREEN)EpicCommon$(NOCOLOR) - Compiling $(words $(java_src_files_common)) + $(words $(java_src_files_null)) source files ..."
	@javac -sourcepath EpicNullPlat/src -implicit:class -d $(CP_COMM) -cp $(CP_BLD) $(JAVAC_PROCESSOR) $(java_src_files_common)
	@rm -rf build/classes_common/com/epic/framework/implementation
	@touch build/.make.EpicCommon
	@#	@echo "$(GREEN)EpicCommon$(NOCOLOR) - jar cf build/EpicCommon.jar $(JAROPT_COMMON)"
	@#	jar cf build/EpicCommon.jar $(JAROPT_COMMON)

.PHONY: EpicDesktop
EpicDesktop: EpicCommon build/.make.EpicDesktop build/EpicBuilder.jar
build/.make.EpicDesktop: $(java_src_files_desktop)
	@mkdir -p $(CP_DESK)
	@echo "$(GREEN)EpicDesktop$(NOCOLOR) - Compiling $(words $(java_src_files_desktop)) source files ..."
	javac -d $(CP_DESK) -cp build/EpicBuilder.jar:$(CP_COMM):$(call classpathify,EpicDesktop/lib/*.jar) $(JAVAC_PROCESSOR) $(java_src_files_desktop)
	@touch build/.make.EpicDesktop

.PHONY: EpicIos
EpicIos: EpicCommon build/.make.EpicIos build/EpicBuilder.jar
build/.make.EpicIos: $(java_src_files_ios)
	@mkdir -p $(CP_IOS)
	@echo "$(GREEN)EpicIos$(NOCOLOR) - Compiling $(words $(java_src_files_ios)) source files ..."
	javac -d $(CP_IOS) -cp build/EpicBuilder.jar:xmlvm/xmlvm.jar:$(CP_COMM):$(call classpathify,EpicIos/lib/*.jar) $(JAVAC_PROCESSOR) $(java_src_files_ios)
	@touch build/.make.EpicIos
	
.PHONY: EpicBuilder.jar
EpicBuilder.jar: EpicBuilder build/EpicBuilder.jar
build/EpicBuilder.jar: build/.make.EpicBuilder $(shell find EpicBuilder/src)
	@echo "$(GREEN)EpicBuilder.jar$(NOCOLOR) - jar cf build/EpicBuilder.jar -C build/classes_builder . -C EpicBuilder/src ."
	@rsync -a EpicBuilder/src/ $(CP_BLD)/
	@jar cf build/EpicBuilder.jar -C build/classes_builder .



.PHONY: EpicDesktop.jar
EpicDesktop.jar: EpicDesktop build/EpicDesktop.jar
build/EpicDesktop.jar: build/.make.EpicDesktop build/.make.EpicCommon build/.make.EpicBuilder $(shell find EpicBuilder/src EpicCommon/src/ EpicDesktop/src/)
	@echo "$(GREEN)EpicDesktop.jar$(NOCOLOR) - Merging dependency jars into build/classes_desktop"
	@cd build/classes_desktop && for j in ../../EpicDesktop/lib/*.jar; do jar xf $$j; done
	rsync -a $(CP_BLD)/ $(CP_DESK)/
	rsync -a $(CP_COMM)/ $(CP_DESK)/
	rsync -a EpicBuilder/src/ $(CP_DESK)/
	rsync -a EpicCommon/src/ $(CP_DESK)/
	rsync -a EpicDesktop/src/ $(CP_DESK)/
	@echo "$(GREEN)EpicDesktop.jar$(NOCOLOR) - jar cf build/EpicDesktop.jar -C build/classes_desktop ."
	@jar cf build/EpicDesktop.jar -C build/classes_desktop .

.PHONY: EpicIos.jar
EpicIos.jar: EpicIos build/EpicIos.jar
build/EpicIos.jar: build/.make.EpicIos build/.make.EpicCommon build/.make.EpicBuilder $(shell find EpicBuilder/src EpicCommon/src/ EpicIos/src/)
	rsync -a $(CP_COMM)/ $(CP_IOS)/
	rsync -a EpicCommon/src/ $(CP_IOS)/
	rsync -a EpicIos/src/ $(CP_IOS)/
	@echo "$(GREEN)EpicIos.jar$(NOCOLOR) - jar cf build/EpicIos.jar -C build/classes_ios ."
	@jar cf build/EpicIos.jar -C build/classes_ios .

.PHONY: jcommander
jcommander: EpicBuilder/lib/jcommander-1.24-SNAPSHOT-bundle.jar
EpicBuilder/lib/jcommander-1.24-SNAPSHOT-bundle.jar: 
	third-party/jcommander/build-with-maven
	cp third-party/jcommander/target/jcommander-*-bundle.jar EpicBuilder/lib/
