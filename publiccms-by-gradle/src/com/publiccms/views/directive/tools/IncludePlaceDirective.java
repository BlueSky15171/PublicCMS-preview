package com.publiccms.views.directive.tools;

import static com.publiccms.logic.component.template.TemplateComponent.INCLUDE_DIRECTORY;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.publiccms.common.base.AbstractTemplateDirective;
import com.publiccms.entities.sys.SysSite;
import com.publiccms.logic.component.template.MetadataComponent;
import com.publiccms.logic.component.template.TemplateComponent;
import com.publiccms.views.pojo.CmsPlaceMetadata;
import com.sanluan.common.handler.RenderHandler;

import freemarker.template.TemplateException;

@Component
public class IncludePlaceDirective extends AbstractTemplateDirective {
    private static ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void execute(RenderHandler handler) throws IOException, Exception {
        String path = handler.getString("path");
        String[] paths = handler.getStringArray("paths");
        if (notEmpty(path)) {
            SysSite site = getSite(handler);
            CmsPlaceMetadata metadata = metadataComponent
                    .getPlaceMetadata(siteComponent.getWebTemplateFilePath(site, INCLUDE_DIRECTORY + path));
            if (site.isUseSsi()) {
                handler.print("<!--#include virtual=\"/" + INCLUDE_DIRECTORY + path + "\"-->");
            } else {
                templateComponent.printPlace(handler.getWriter(), site, path, metadata);
            }
        } else if (notEmpty(paths)) {
            Map<String, String> map = new ConcurrentHashMap<String, String>();
            Set<String> set = new HashSet<String>();
            for (String p : paths) {
                set.add(p);
            }
            for (String p : set) {
                pool.execute(new RanderTask(p, set.size(), map, getSite(handler), handler));
            }
            handler.setRenderd();
        }
    }

    @Override
    public boolean needAppToken() {
        return true;
    }

    @Autowired
    private TemplateComponent templateComponent;
    @Autowired
    private MetadataComponent metadataComponent;

    /**
     * 
     * RanderTask 渲染线程
     *
     */
    class RanderTask implements Runnable {
        private String path;
        private int count;
        private Map<String, String> resultMap;
        private SysSite site;
        private RenderHandler handler;

        public RanderTask(String path, int count, Map<String, String> resultMap, SysSite site, RenderHandler handler) {
            this.path = path;
            this.count = count;
            this.resultMap = resultMap;
            this.site = site;
            this.handler = handler;
        }

        @Override
        public void run() {
            CmsPlaceMetadata metadata = metadataComponent
                    .getPlaceMetadata(siteComponent.getWebTemplateFilePath(site, INCLUDE_DIRECTORY + path));
            if (site.isUseSsi()) {
                resultMap.put(path, "<!--#include virtual=\"/" + INCLUDE_DIRECTORY + path + "\"-->");
            } else {
                try {
                    resultMap.put(path, templateComponent.printPlace(site, path, metadata));
                } catch (IOException | TemplateException e) {
                    log.error(e.getMessage());
                    resultMap.put(path, BLANK);
                }
            }
            synchronized (this) {
                if (resultMap.size() >= count) {
                    try {
                        handler.put("map", resultMap).render();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }
                }
            }
        }
    }
}
