#import "java_lang_String.h"
#import "com_epic_framework_common_util_exceptions_EpicNativeMethodMissingImplementation.h"

// Automatically generated by xmlvm2obj. Do not edit!


#import "com_epic_framework_implementation_EpicIOImplementationNative.h"
#import "org_xmlvm_iphone_NSObject.h"


@implementation com_epic_framework_implementation_EpicIOImplementationNative;

NSString *getFullPath(java_lang_String *filename) {
  NSArray *appDocumentPaths = NSSearchPathForDirectoriesInDomains(
    NSDocumentDirectory,
    NSUserDomainMask, 
    YES
  );
  NSString *docsDirectory = [appDocumentPaths objectAtIndex: 0];
  NSString *fullPathToFile = [docsDirectory stringByAppendingPathComponent: filename];
  NSLog(@"filename=%@", fullPathToFile);
  return fullPathToFile;
}

+ (void) getFullPath___java_lang_String
  : (java_lang_String*) filename
{
  return [getFullPath(filename) retain];  
}

+ (void) writeFile___java_lang_String_byte_ARRAYTYPE
  : (java_lang_String*) filename
  : (XMLVMArray*) bytes
{
  NSString *fullPath = getFullPath(filename);
  NSData *data = [NSData dataWithBytesNoCopy: bytes->array.b length: bytes->length freeWhenDone: NO];
  if ([data writeToFile: fullPath atomically: YES])
    NSLog(@"%@ saved.", fullPath);
  else
    NSLog(@"Error writing %@", fullPath);
}
#define BYTE_TYPE 3
+ (XMLVMArray*) readFile___java_lang_String
  : (java_lang_String*) filename
{
  return NULL;
  NSString *fullPath = getFullPath(filename);
  NSData *data = [NSData dataWithContentsOfFile: fullPath];
  
  if(data) {
    XMLVMArray *array = [XMLVMArray createSingleDimensionWithType: BYTE_TYPE size: [data length] andData: [data bytes]];
    [data release];
    NSLog(@"Read %d bytes from file %@", [data length], fullPath);
    return array;
  } else {
    NSLog(@"Error reading %@", fullPath);
  }

}

@end

