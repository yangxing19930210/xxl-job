package com.xuxueli.executor.sample.nutz;

import org.nutz.mvc.annotation.*;
import org.nutz.mvc.ioc.provider.ComboIocProvider;

import com.xuxueli.executor.sample.nutz.config.NutzSetup;

/**
 * nutz module
 *
 * @author xuxueli 2017-12-25 17:58:43
 */
@IocBy(type = ComboIocProvider.class,
    args = {"*org.nutz.ioc.loader.annotation.AnnotationIocLoader", "com.xuxueli.executor.sample.nutz"})
@Encoding(input = "utf-8", output = "utf-8")
@Modules(scanPackage = true)
@Localization("msg")
@Ok("json")
@Fail("json")
@SetupBy(NutzSetup.class)
public class MainModule {

}
