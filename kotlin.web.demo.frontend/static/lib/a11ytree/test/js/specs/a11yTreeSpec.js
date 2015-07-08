
describe('a11yTree plugin', function () {
    const MAIN_SELECTOR = '#main';
    const LIST_ITEM_SELECTOR = 'li';
    const LEVEL_1_ID = 'level-1', LEVEL_1_ID_SELECTOR = '#' + LEVEL_1_ID;
    const LEVEL_2_ID = 'level-2', LEVEL_2_ID_SELECTOR = '#' + LEVEL_2_ID;
    const LEVEL_3_ID = 'level-3', LEVEL_3_ID_SELECTOR = '#' + LEVEL_3_ID;
    const NO_CHILDREN_CLASS = 'at-no-children', NO_CHILDREN_CLASS_SELECTOR = '.' + NO_CHILDREN_CLASS;
    const HAS_CHILDREN_CLASS = 'at-has-children', HAS_CHILDREN_CLASS_SELECTOR = '.' + HAS_CHILDREN_CLASS;
    const TOGGLE_CLASS_SELECTOR = '.at-toggle';

    var $firstLevel1Item, $firstLevel2Item, $secondLevel1Item, $secondLevel2Item, $firstLevel3Item, $secondLevel3Item;

    beforeEach(function () {
        var htmlContent = '<div id="main"></div>';
        $('body').append(htmlContent);
        $('#main').append('<h2 id="this-tree-label">My Tree</h2>');
        appendList(MAIN_SELECTOR, LEVEL_1_ID, 2);
        appendList(LEVEL_1_ID_SELECTOR + ' > li:nth-child(1)', LEVEL_2_ID, 2);
        appendList(LEVEL_2_ID_SELECTOR + ' > li:nth-child(1)', LEVEL_3_ID, 2);

        $firstLevel1Item = getNthItemInList(LEVEL_1_ID_SELECTOR, 1);
        $firstLevel2Item = getNthItemInList(LEVEL_2_ID_SELECTOR, 1);
        $secondLevel1Item = getNthItemInList(LEVEL_1_ID_SELECTOR, 2);
        $secondLevel2Item = getNthItemInList(LEVEL_2_ID_SELECTOR, 2);
        $firstLevel3Item = getNthItemInList(LEVEL_3_ID_SELECTOR, 1);
        $secondLevel3Item = getNthItemInList(LEVEL_3_ID_SELECTOR, 2);
    });

    afterEach(function () {
        $(MAIN_SELECTOR).remove();
    });

    describe('used on a parent ul element', function () {

        beforeEach(function () {
            $(LEVEL_1_ID_SELECTOR).a11yTree();
        });

        it('identifies the parent tree', function () {
            expect($('ul[role="tree"]').length).toBe(1);
            expect($(LEVEL_1_ID_SELECTOR).attr('role')).toBe('tree');
        });

        it('identifies all tree items', function () {
            expect($('[role="treeitem"]').length).toBe($('li[role="treeitem"]').length);
        });


        it('identifies the appropriate nested level for each tree item', function () {
            expect($('[aria-level]').length).toBe(6);
            verifyAriaLevelForChildren(LEVEL_1_ID_SELECTOR, 1, 2);
            verifyAriaLevelForChildren(LEVEL_2_ID_SELECTOR, 2, 2);
            verifyAriaLevelForChildren(LEVEL_3_ID_SELECTOR, 3, 2);
        });

        it('identifies items with no children', function () {
            expect($(NO_CHILDREN_CLASS_SELECTOR).length).toBe(4);
            verifyClassForChildren(LEVEL_1_ID_SELECTOR, 2, NO_CHILDREN_CLASS);
            verifyClassForChildren(LEVEL_2_ID_SELECTOR, 2, NO_CHILDREN_CLASS);
            verifyClassForChildren(LEVEL_3_ID_SELECTOR, 1, NO_CHILDREN_CLASS);
            verifyClassForChildren(LEVEL_3_ID_SELECTOR, 2, NO_CHILDREN_CLASS);
        });

        it('identifies items with children', function () {
            expect($('ul[role="group"]').length).toBe($(HAS_CHILDREN_CLASS_SELECTOR).length);
            verifyElementHasAttribute(LEVEL_2_ID_SELECTOR, 'role', 'group');
            verifyElementHasAttribute(LEVEL_3_ID_SELECTOR, 'role', 'group');
            verifyClassForChildren(LEVEL_1_ID_SELECTOR, 1, HAS_CHILDREN_CLASS);
            verifyClassForChildren(LEVEL_2_ID_SELECTOR, 1, HAS_CHILDREN_CLASS);
        });

        it('items with children are collapsed by default', function () {
            hasItemsCollapsed($(HAS_CHILDREN_CLASS_SELECTOR).length);
            isCollapsed($firstLevel1Item);
            isCollapsed($firstLevel2Item);
        });

        describe('has navigation', function() {

            describe('item selection', function() {

                it('initializes first item as selected', function() {
                    isOnlyItemInFocus($firstLevel1Item);
                });

                it('sets the item in focus as selected', function() {
                    focusOnItem($firstLevel1Item);
                    isOnlyItemInFocus($firstLevel1Item);
                });

                it('only one item at a time can be selected', function() {
                    focusOnItem($firstLevel1Item);
                    focusOnItem($firstLevel2Item);
                    isOnlyItemInFocus($firstLevel2Item);
                });

                it('marks item as selected when clicking on it', function() {
                    $firstLevel2Item.click();
                    isOnlyItemInFocus($firstLevel2Item);
                });
            });

            describe('using toggle controls', function() {

                it('adds toggle control to items with children', function () {
                    expect($(TOGGLE_CLASS_SELECTOR).length).toBe(2);
                    verifyItemHasToggle($firstLevel1Item);
                    verifyItemHasToggle($firstLevel2Item);
                });

                it('clicking collapsed toggle expands only direct children', function () {
                    $firstLevel1Item.children(TOGGLE_CLASS_SELECTOR).click();
                    hasItemsExpanded(1);
                    isExpanded($firstLevel1Item);
                });

                it('clicking expanded toggle collapses only direct children', function () {
                    $firstLevel1Item.children(TOGGLE_CLASS_SELECTOR).click();
                    $firstLevel2Item.children(TOGGLE_CLASS_SELECTOR).click();
                    $firstLevel1Item.children(TOGGLE_CLASS_SELECTOR).click();
                    hasItemsCollapsed(1);
                    hasItemsExpanded(1);
                    isCollapsed($firstLevel1Item);
                    isExpanded($firstLevel2Item);
                });

            });

            describe('using the keyboard', function() {

                describe('tabindex', function() {
                    it('adds only main tree item to the tab order', function() {
                        expect($(LEVEL_1_ID_SELECTOR).attr('tabindex')).toBe('0');
                        expect($(LEVEL_1_ID_SELECTOR).find('li[tabindex="0"]').length).toBe(0);
                    });
                });

                describe('using the down arrow key', function() {

                    it('focuses on the next sibling item in the tree if current item in focus is collapsed or has no children', function() {
                        focusOnItem($firstLevel1Item);
                        downArrowKey();
                        isOnlyItemInFocus($secondLevel1Item);
                    });

                    it('focuses on first child item in the tree if current item in focus has children and is expanded', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        downArrowKey();
                        isOnlyItemInFocus($firstLevel2Item);
                    });

                    it('focuses on next parent item in the tree if current item is the last child item of the sibling parent', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        downArrowKey();
                        downArrowKey();
                        downArrowKey();
                        isOnlyItemInFocus($secondLevel1Item);
                    });

                    it('focuses on next available parent item in the tree if current item is the last child item of the current parent', function() {
                        $secondLevel2Item.remove();
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        downArrowKey();
                        rightArrowKey();
                        downArrowKey();
                        downArrowKey();
                        downArrowKey();
                        isOnlyItemInFocus($secondLevel1Item);
                    });

                });

                describe('using the up arrow key', function() {

                    it('focuses on the previous sibling element in the tree if the previous sibling is collapsed or has no children', function() {
                        focusOnItem($secondLevel1Item);
                        upArrowKey();
                        isOnlyItemInFocus($firstLevel1Item);
                    });

                    it('focuses on the last child item of the previous sibling in the tree if the previous sibling has children and is expanded', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        focusOnItem($secondLevel1Item);
                        upArrowKey();
                        isOnlyItemInFocus($secondLevel2Item);
                    });

                    it('focuses on the parent if the item in focus is the first child of an item', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        downArrowKey();
                        upArrowKey();
                        isOnlyItemInFocus($firstLevel1Item);
                    });

                    it('focuses on the last visible child item of the previous sibling tree if the previous sibling has children and is expanded', function() {
                        $secondLevel2Item.remove();
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        downArrowKey();
                        rightArrowKey();
                        downArrowKey();
                        downArrowKey();
                        downArrowKey();
                        upArrowKey();
                        isOnlyItemInFocus($secondLevel3Item);
                    });
                });

                describe('using the right arrow key', function() {
                    it('expands the child list when exists in the tree', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        isOnlyItemInFocus($firstLevel1Item);
                        isExpanded($firstLevel1Item);
                    });

                    it('focuses on the first child in a list when exists', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        rightArrowKey();
                        isOnlyItemInFocus($firstLevel2Item);
                    });
                });

                describe('using the left arrow key', function() {
                    it('collapses the child list when exists in the tree', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        leftArrowKey();
                        isOnlyItemInFocus($firstLevel1Item);
                        isCollapsed($firstLevel1Item);
                    });

                    it('focuses on the parent item if current item in list has no children', function() {
                        focusOnItem($firstLevel1Item);
                        rightArrowKey();
                        rightArrowKey();
                        rightArrowKey();
                        rightArrowKey();
                        isOnlyItemInFocus($firstLevel3Item);
                        leftArrowKey();
                        isOnlyItemInFocus($firstLevel2Item);
                    });
                });
                describe('using the Enter key', function() {
                    it('expands the current selected element if it has children and is collapsed', function() {
                        focusOnItem($firstLevel1Item);
                        enterKey();
                        isExpanded($firstLevel1Item);
                    });

                    it('collapses the current selected element if it has children and is expanded', function() {
                        focusOnItem($firstLevel1Item);
                        enterKey();
                        enterKey();
                        isCollapsed($firstLevel1Item);
                    });
                });

                describe('using the end key', function() {
                    it('selects the last expanded element when the element is the last element of the top tree level', function() {
                        focusOnItem($firstLevel1Item);
                        endKey();
                        isOnlyItemInFocus($secondLevel1Item);
                    });

                    it('selects the last expanded element when the element is the last element of the last expanded element', function() {
                        focusOnItem($firstLevel1Item);
                        $secondLevel1Item.remove();
                        enterKey();
                        downArrowKey();
                        isOnlyItemInFocus($firstLevel2Item);
                        endKey();
                        isOnlyItemInFocus($secondLevel2Item);
                    });

                    describe('tree with sub trees in last main tree item', function() {

                        beforeEach(function() {
                            $(LEVEL_1_ID_SELECTOR).remove();
                            appendList(MAIN_SELECTOR, LEVEL_1_ID, 2);
                            appendList(LEVEL_1_ID_SELECTOR + ' > li:nth-child(1)', LEVEL_2_ID, 2);
                            appendList(LEVEL_2_ID_SELECTOR + ' > li:nth-child(1)', LEVEL_3_ID, 2);
                            appendList(LEVEL_1_ID_SELECTOR + ' > li:nth-child(2)', LEVEL_2_ID + 'b', 2);
                            $firstLevel1Item = getNthItemInList(LEVEL_1_ID_SELECTOR, 1);
                            $firstLevel2Item = getNthItemInList(LEVEL_2_ID_SELECTOR, 1);
                            $secondLevel1Item = getNthItemInList(LEVEL_1_ID_SELECTOR, 2);
                            $secondLevel2Item = getNthItemInList(LEVEL_2_ID_SELECTOR, 2);
                            $firstLevel3Item = getNthItemInList(LEVEL_3_ID_SELECTOR, 1);
                            $secondLevel3Item = getNthItemInList(LEVEL_3_ID_SELECTOR, 2);
                        });

                        it('selects the last expanded element when the element is the last element of the last expanded element where last node has children and is collapsed', function() {
                            $(LEVEL_1_ID_SELECTOR).a11yTree();
                            focusOnItem($firstLevel1Item);
                            isOnlyItemInFocus($firstLevel1Item);
                            endKey();
                            isOnlyItemInFocus($secondLevel1Item);
                        });

                        it('selects the last expanded element when the element is the last element of the last expanded element where last node has children and is collapsed, but children list is expanded', function() {
                            appendList(LEVEL_2_ID_SELECTOR + 'b > li:nth-child(2)', LEVEL_3_ID + 'b', 2);
                            $(LEVEL_1_ID_SELECTOR).a11yTree();
                            focusOnItem($firstLevel1Item);
                            isOnlyItemInFocus($firstLevel1Item);
                            downArrowKey();
                            rightArrowKey();
                            downArrowKey();
                            downArrowKey();
                            rightArrowKey();
                            upArrowKey();
                            upArrowKey();
                            leftArrowKey();
                            upArrowKey();
                            endKey();
                            isOnlyItemInFocus($secondLevel1Item);
                        });
                    });
                });

                describe('using the home key', function() {
                    it('selects the first element of the tree', function() {
                        focusOnItem($firstLevel1Item);
                        enterKey();
                        downArrowKey();
                        isOnlyItemInFocus($firstLevel2Item);
                        homeKey();
                        isOnlyItemInFocus($firstLevel1Item);
                    });
                });
            });
        });
    });

    describe('keeps track of the item selected at the tree level', function() {
        it('adds an id to all elements in tree', function() {
            expect($(LEVEL_1_ID_SELECTOR).find('li[id]').length).toBe(0);
            $(LEVEL_1_ID_SELECTOR).a11yTree();
            expect($(LEVEL_1_ID_SELECTOR).find('li[id]').length).toBe($(LEVEL_1_ID_SELECTOR).find('li').length);
        });

        it('uses exiting element id if element in tree already has an id', function() {
            $(LEVEL_1_ID_SELECTOR).children('li:first-child').attr('id','tree-item-1');
            $(LEVEL_1_ID_SELECTOR).a11yTree();
            expect($('#tree-item-1').length).toBe(1);
        });

        it('constructs list element id using element level and sibling position', function() {
            $(LEVEL_1_ID_SELECTOR).a11yTree();
            expect($('#at-level-1-n-0').length).toBe(1);
            expect($('#at-level-1-n-0-0').length).toBe(1);
            expect($('#at-level-1-n-0-0-0').length).toBe(1);
            expect($('#at-level-1-n-0-0-1').length).toBe(1);
            expect($('#at-level-1-n-0-1').length).toBe(1);
            expect($('#at-level-1-n-1').length).toBe(1);
        });

        it('updates aria-activedescendant with item selected on change', function() {
            $(LEVEL_1_ID_SELECTOR).a11yTree();
            expect($(LEVEL_1_ID_SELECTOR).attr('aria-activedescendant')).toBe('at-level-1-n-0');
            downArrowKey();
            expect($(LEVEL_1_ID_SELECTOR).attr('aria-activedescendant')).toBe('at-level-1-n-1');
        });

        it('does not use tree id as prefix if tree id does not exist', function() {
            $(LEVEL_1_ID_SELECTOR).removeAttr('id');
            $(MAIN_SELECTOR + '> ul').a11yTree();
            expect($('#at-n-0').length).toBe(1);
            expect($('#at-n-0-0').length).toBe(1);
            expect($('#at-n-0-0-0').length).toBe(1);
            expect($('#at-n-0-0-1').length).toBe(1);
            expect($('#at-n-0-1').length).toBe(1);
            expect($('#at-n-1').length).toBe(1);
        });

    });

    describe('has options', function() {
        describe('insertToggle', function() {
            it('inserts toggle into list elements with children by default', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree();
                expect($('.at-toggle').length).toBe(2);
            });

            it('does not insert toggle into DOM when set to false', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree({insertToggle : false});
                expect($('.at-toggle').length).toBe(0);
            });
        });

        describe('toggleSelector', function() {
            beforeEach(function() {
                $(LEVEL_1_ID_SELECTOR).find('ul').parent('li').append('<i class="fa fa-plus-square-o"></i>');
                $(LEVEL_1_ID_SELECTOR).a11yTree({toggleSelector : 'i.fa'});
            });

            it('adds plugin toggle class to existing toggle', function() {
                expect($('i.fa.at-toggle').length).toBe(2);
            });

            it('hides existing toggle from assistive technology', function() {
                expect($('i.fa.at-toggle[aria-hidden="true"]').length).toBe(2);
            });
        });

        describe('treeLabelId', function() {
            it('associates tree with label when exists', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree({treeLabelId :'this-tree-label'});
                expect($(LEVEL_1_ID_SELECTOR).attr('aria-labelledby')).toBe('this-tree-label');
            });

            it('does not associate tree with label if does not exists', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree({treeLabelId :'this-tree-label-no-exist'});
                expect($(LEVEL_1_ID_SELECTOR).attr('aria-labelledby')).toBe(undefined);
            });

        });

        describe('treeItemLabelSelector', function() {
            it('adds a unique id to each item found by selector', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree({treeItemLabelSelector :'.tree-item-node'});
                expect($('#at-level-1-n-0-label').length).toBe(1);
                expect($('#at-level-1-n-0-0-label').length).toBe(1);
                expect($('#at-level-1-n-0-0-0-label').length).toBe(1);
                expect($('#at-level-1-n-0-0-1-label').length).toBe(1);
                expect($('#at-level-1-n-0-1-label').length).toBe(1);
                expect($('#at-level-1-n-1-label').length).toBe(1);
            });

            it('adds aria-labelledby only to items that have children', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree({treeItemLabelSelector :'.tree-item-node'});
                expect($(LEVEL_1_ID_SELECTOR).find('[aria-labelledby]').length).toBe(2);
                expect($(LEVEL_2_ID_SELECTOR).attr('aria-labelledby')).toBe('at-level-1-n-0-label');
                expect($(LEVEL_3_ID_SELECTOR).attr('aria-labelledby')).toBe('at-level-1-n-0-0-label');
            });
        });

        describe('customToggle', function() {
            it('is default to undefined', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree();
                expect($('.at-toggle').children().length).toBe(0);
            });

            it('inserts custom customToggle.html when defined', function() {
                $(LEVEL_1_ID_SELECTOR).a11yTree(
                    {
                        customToggle :{
                            html:'<i class="fa fa-plus-square-o"></i>'
                        }
                    }
                );
                expect($('.fa-plus-square-o').length).toBe(2);
            });


        });

        describe('onCollapse and onExpand callbacks', function() {
            var collapseCallback, expandCallback;

            beforeEach(function() {
                collapseCallback = jasmine.createSpy();
                expandCallback = jasmine.createSpy();
                $(LEVEL_1_ID_SELECTOR).a11yTree(
                    {
                        onCollapse: collapseCallback,
                        onExpand: expandCallback
                    }
                );
                focusOnItem($firstLevel1Item);
            });

            it('left arrow key onCollapse triggers custom toggle collapse with event when defined', function() {
                rightArrowKey();
                leftArrowKey();
                expect(collapseCallback).toHaveBeenCalledWith(jasmine.any(Object), jasmine.any(Object));
            });

            it('right arrow key onExpand triggers custom toggle expand with event when defined', function() {
                rightArrowKey();
                expect(expandCallback).toHaveBeenCalledWith(jasmine.any(Object), jasmine.any(Object));
            });

            it('enter key triggers custom toggle expand with event when defined', function() {
                enterKey();
                expect(expandCallback).toHaveBeenCalledWith(jasmine.any(Object), jasmine.any(Object));
                enterKey();
                expect(collapseCallback).toHaveBeenCalledWith(jasmine.any(Object), jasmine.any(Object));
            });

            xit('clicking toggle triggers custom toggle expand with event when defined', function() {
                var $firstToggle = $(TOGGLE_CLASS_SELECTOR)[0];
                $firstToggle.click();
                expect(expandCallback).toHaveBeenCalledWith(jasmine.any(Object), jasmine.any(Object));
                $firstToggle.click();
                expect(collapseCallback).toHaveBeenCalledWith(jasmine.any(Object), jasmine.any(Object));
            });
        });
    });

    function triggerKeydown(key) {
        var event = $.Event('keydown');
        event.which = key;
        $(LEVEL_1_ID_SELECTOR).trigger(event);
    }

    function downArrowKey() {
        triggerKeydown(40);
    }

    function upArrowKey() {
        triggerKeydown(38);
    }
    
    function rightArrowKey() {
        triggerKeydown(39);
    }

    function leftArrowKey() {
        triggerKeydown(37);
    }
    
    function enterKey() {
        triggerKeydown(13);
    }

    function endKey() {
        triggerKeydown(35);
    }

    function homeKey() {
        triggerKeydown(36);
    }
    
    function focusOnItem($item) {
        $(MAIN_SELECTOR).find('li').attr('aria-selected','false');
        $item.attr('aria-selected','true');
    }

    function isOnlyItemInFocus($item) {
        expect($(MAIN_SELECTOR).find('[aria-selected="true"]').length).toBe(1);
        expect($item.attr('aria-selected')).toBe('true');
    }

    function getNthItemInList(listSelector, idx) {
        return $(listSelector + ' > li:nth-child(' + idx + ')');
    }

    function hasItemsCollapsed(numberOfItems) {
        expect($('li[aria-expanded="false"]').length).toBe(numberOfItems);
    }

    function hasItemsExpanded(numberOfItems) {
        expect($('li[aria-expanded="true"]').length).toBe(numberOfItems);
    }

    function isExpanded($item) {
        expect($item.attr('aria-expanded')).toBe('true');
    }

    function isCollapsed($item) {
        expect($item.attr('aria-expanded')).toBe('false');
    }

    function verifyItemHasToggle($listItem) {
        var $toggle = $listItem.children(TOGGLE_CLASS_SELECTOR);
        expect($toggle.length).toBe(1);
        expect($toggle.attr('aria-hidden')).toBe('true');
        expect($listItem.children(TOGGLE_CLASS_SELECTOR).length).toBe(1);
    }

    function verifyElementHasAttribute(selector, attribute, value) {
        expect($(selector).attr(attribute)).toBe(value);
    }

    function verifyClassForChildren(ulSelector, childIdx, className) {
        expect($(ulSelector + ' > li:nth-child(' + childIdx + ')').hasClass(className)).toBeTruthy();
    }

    function verifyAriaLevelForChildren(ulSelector, level, count) {
        expect($(ulSelector + ' > li[aria-level="' + level + '"]').length).toBe(count);
    }

    function appendList(parentSelector, listId, numberOfListItems) {
        var listHtml = '<ul id="' + listId + '">' + createListItems(listId, numberOfListItems) + '</ul>';
        $(parentSelector).append(listHtml);
    }

    function createListItems(listId, numberOfListItems) {
        var listItemsHtml = '';
        for (var listItem = 1; listItem <= numberOfListItems; listItem++) {
            listItemsHtml += '<li><span class="tree-item-node">' + listId + ' ' + listItem + '</span></li>';
        }
        return listItemsHtml;
    }
});