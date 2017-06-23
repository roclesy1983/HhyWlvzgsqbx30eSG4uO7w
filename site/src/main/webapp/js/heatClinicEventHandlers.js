/**
 * Reveal the Product's quickview link
 */
$('body').on('mouseenter', '.product_container .image', function() {
    HC.toggleQuickview($(this), true);
});

/**
 * Hide the Product's quickview link
 */
$('body').on('mouseleave', '.product_container .image', function() {
    HC.toggleQuickview($(this), false);
});

/**
 * Handle the Product's quickview link click & show the quickview modal
 */
$('body').on('click', '.js-quickview', function() {
    var link = $(this).closest('.image').find('.imageLink').attr('href');
    BLC.ajax({
        url: link + "?quickview=true"
    }, function(data) {
        data.find('.product-options').removeClass('hidden');
        data.find('.product-option-nonjs').remove();
        $.modal(data.find('#left_column')[0], HC.quickviewOptions);
        $('#simplemodal-container').find('.jqzoom').jqzoom({
            zoomType: 'innerzoom', 
            zoomWidth: 402,
            zoomHeight: 402,
            title: false
        });

        
        // Reinitialize AddThis for modal that was loaded;
        // attributes addthis:title and addthis:url (on product pages) cannot be set with thymeleaf because of the 
        // colon, so we fill a dummy attribute and copy it over using jquery
        $('div[addthistitle]').each(function(){
            $(this).attr('addthis:title', $(this).attr('addthistitle'));
            $(this).attr('addthistitle', null);
        });
        $('div[addthisurl]').each(function(){
            $(this).attr('addthis:url', $(this).attr('addthisurl'));
            $(this).attr('addthisurl', null);
        });
        if(typeof(addthis) !== 'undefined') {
            window.addthis.toolbox('.addthis_toolbox');
        }
    });
    return false;
});
