<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

</div> <!-- END #content -->

<footer>

</footer>

</div> <!-- END #container -->

<script src="https://cdnjs.cloudflare.com/ajax/libs/headjs/1.0.3/head.min.js"></script>
<spring:theme code="cas.javascript.file" var="casJavascriptFile" text="" />
<script type="text/javascript" src="<c:url value="${casJavascriptFile}" />"></script>
<script type="text/javascript" src="<c:url value="/js/custom.js" />"></script>

</body>
</html>

