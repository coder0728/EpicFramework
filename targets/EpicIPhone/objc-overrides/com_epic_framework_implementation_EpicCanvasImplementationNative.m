#import "java_lang_String.h"
#import "com_epic_framework_common_util_exceptions_EpicNativeMethodMissingImplementation.h"
#import "org_xmlvm_iphone_UIImage.h"
#import "org_xmlvm_iphone_CGContext.h"
#import "com_epic_framework_implementation_EpicCanvasImplementationNative.h"
#import "org_xmlvm_iphone_NSObject.h"


@implementation com_epic_framework_implementation_EpicCanvasImplementationNative;
+ (org_xmlvm_iphone_UIImage*) drawImage___org_xmlvm_iphone_UIImage_int_int_int
  : (org_xmlvm_iphone_UIImage*) image
  : (int) left
  : (int) top
  : (int) alpha
{
  UIImage* uii = (UIImage*) image;
  CGPoint point = CGPointMake(left, top);
  float falpha = alpha / 255.0f;
  [uii drawAtPoint: point blendMode: kCGBlendModeNormal alpha: falpha];
}

+ (void) setCrop___org_xmlvm_iphone_CGContext_int_int_int_int
  : (org_xmlvm_iphone_CGContext*) context 
  : (int) left
  : (int) top
  : (int) width
  : (int) height
{
  CGRect clipRect = CGRectMake(left, top, width, height);
  CGContextSaveGState(context->context);
  CGContextClipToRect(context->context, clipRect);
}

+ (void) restoreContext___org_xmlvm_iphone_CGContext
  : (org_xmlvm_iphone_CGContext*) context
{
  CGContextRestoreGState(context->context);
}

@end

