<%@ page session="false" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="page-header">
    <h3>
        ${_res.dashboard}
    </h3>
</div>
<div class="alert alert-block alert-info">
    <i class="fa fa-info">&nbsp;</i>
    ${_res['admin.index.welcomeTips']}
</div>
<div class="row">
    <div class="col-md-7 col-xs-12 col-sm-12">
        <div class="widget-box">
            <div class="widget-header">
                <h4>
                    <i class="fa fa-server"></i>
                    ${_res.serverInfo}
                </h4>
            </div>
            <div class="widget-body">
                <div class="widget-main">
                    <table id="sample-table-1" class="table table-striped table-bordered table-hover">
                        <thead>
                        <tr>
                            <th class="hidden-480">${_res.key}</th>

                            <th>
                                ${_res.value}
                            </th>
                        </tr>
                        </thead>

                        <tbody>
                        <tr>
                            <td>
                                ${_res['admin.text.env']}
                            </td>
                            <td>${system['java.vm.name']} (${system['java.runtime.version']})</td>
                        </tr>
                        <tr>
                            <td>
                                ${_res['admin.text.container']}
                            </td>
                            <td>${system['server.info']}</td>
                        </tr>
                        <tr>
                            <td>
                                ${_res['admin.text.path']}
                            </td>
                            <td>${system['zrlog.runtime.path']}</td>
                        </tr>
                        <tr>
                            <td>
                                ${_res['admin.text.os']}
                            </td>
                            <td><i class="fa fa-${fn:toLowerCase(system['os.name'])}"></i> ${system['os.name']}
                                - ${system['os.arch']}
                                - ${system['os.version']}</td>
                        </tr>
                        <tr>
                            <td>
                                ${_res['admin.text.region']}
                            </td>
                            <td>${system['user.timezone']} - ${system['user.country']}/${system['user.language']}</td>
                        </tr>
                        <tr>
                            <td>
                                ${_res['admin.text.database.version']}
                            </td>
                            <td>${system['dbServer.version']}</td>
                        </tr>
                        <tr>
                            <td>
                                ${_res['admin.text.os.encode']}
                            </td>
                            <td>${system['file.encoding']}</td>
                        </tr>
                        <tr>
                            <td>
                                ${_res['admin.text.service.version']}
                            </td>
                            <td>${zrlog.version} - ${zrlog.buildId} (${zrlog.buildTime})</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    <div class="col-md-5 col-xs-12 col-sm-12">
        <div class="widget-box">
            <div class="widget-header">
                <h4>
                    <i class="fa fa-pie-chart"></i>
                    ${_res['admin.index.outline']}
                </h4>
            </div>
            <div class="widget-body">
                <div class="widget-main">
                    <div class="row">
                        <div class="col-md-6 col-sm-12 col-xs-12">
                            <div class="tile-stats">
                                <div class="icon"><i class="fa fa-comments-o"></i></div>
                                <div class="count">${toDayCommCount}</div>
                                <h3>${_res['admin.text.comments.today']}</h3>
                            </div>
                        </div>
                        <div class="col-md-6 col-sm-12 col-xs-12">
                            <div class="tile-stats">
                                <div class="icon"><i class="fa fa-comments-o"></i></div>
                                <div class="count">${commCount }</div>
                                <h3>${_res['admin.text.comments.total']}</h3>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 col-sm-12 col-xs-12">
                            <div class="tile-stats">
                                <div class="icon"><i class="fa fa-newspaper-o"></i></div>
                                <div class="count">${articleCount }</div>
                                <h3>${_res['admin.text.articles.total']}</h3>
                            </div>
                        </div>
                        <div class="col-md-6 col-sm-12 col-xs-12">
                            <div class="tile-stats">
                                <div class="icon"><i class="fa fa-eye"></i></div>
                                <div class="count">${clickCount }</div>
                                <h3>${_res['admin.text.views.total']}</h3>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
${pageEndTag}