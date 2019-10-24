/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spec.cuba.web.components.datagrid

import com.haulmont.cuba.gui.components.DataGrid
import com.haulmont.cuba.gui.components.data.datagrid.ContainerDataGridItems
import com.haulmont.cuba.gui.model.CollectionContainer
import com.haulmont.cuba.gui.screen.OpenMode
import com.haulmont.cuba.web.gui.components.WebDataGrid
import com.haulmont.cuba.web.testmodel.sample.GoodStatistic
import spec.cuba.web.UiScreenSpec
import spec.cuba.web.components.datagrid.screens.DataGridAggregationScreen

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
class DataGridAggregationTest extends UiScreenSpec {

    private CollectionContainer<GoodStatistic> container

    void setup() {
        exportScreensPackages(['spec.cuba.web.components.datagrid.screens'])
    }

    def "add header at index"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dataGridScreen = screens.create(DataGridAggregationScreen)
        dataGridScreen.show()

        def dataGrid = (WebDataGrid) dataGridScreen.getWindow().getComponent("aggregationTopDataGrid")

        when:
        setDataGridItems(dataGrid)
        then:
        dataGrid.headerRows.size() == 2

        // check that we cannot get aggregation row
        when:
        dataGrid.getHeaderRow(1)
        then:
        thrown(IndexOutOfBoundsException)

        when:
        def header = (DataGrid.HeaderRow) dataGrid.addHeaderRowAt(1)
        def addedHeader = dataGrid.getHeaderRow(1)
        then:
        addedHeader == header

        // we cannot add header row at 3 because for now it must contain only two header rows
        when:
        dataGrid.addHeaderRowAt(3)
        then:
        thrown(IndexOutOfBoundsException)
    }

    def "append header row"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dataGridScreen = screens.create(DataGridAggregationScreen)
        dataGridScreen.show()

        def dataGrid = (WebDataGrid) dataGridScreen.getWindow().getComponent("aggregationTopDataGrid")

        when:
        setDataGridItems(dataGrid)
        then:
        dataGrid.headerRows.size() == 2

        // check that appended row is above than aggregation
        when:
        def header = dataGrid.appendHeaderRow()
        def addedHeader = dataGrid.getHeaderRow(1)
        then:
        header == addedHeader
    }

    def "add footer row at index"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dataGridScreen = screens.create(DataGridAggregationScreen)
        dataGridScreen.show()

        def dataGrid = (WebDataGrid) dataGridScreen.getWindow().getComponent("aggregationBottomDataGrid")

        when:
        setDataGridItems(dataGrid)
        then:
        dataGrid.footerRows.size() == 1

        // check that we cannot get aggregation row
        when:
        dataGrid.getFooterRow(0)
        then:
        thrown(IndexOutOfBoundsException)

        when:
        def footer = dataGrid.addFooterRowAt(0)
        def addedFooter = dataGrid.getFooterRow(0)
        then:
        footer == addedFooter

        // we cannot add footer at 2 because for now it must contain only one footer
        when:
        dataGrid.addFooterRowAt(2)
        then:
        thrown(IndexOutOfBoundsException)
    }

    def "prepend footer row"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        def dataGridScreen = screens.create(DataGridAggregationScreen)
        dataGridScreen.show()

        def dataGrid = (WebDataGrid) dataGridScreen.getWindow().getComponent("aggregationBottomDataGrid")

        when:
        setDataGridItems(dataGrid)
        then:
        dataGrid.footerRows.size() == 1

        // prepended footer should be below than aggregation
        when:
        def footer = dataGrid.prependFooterRow()
        def addedFooter = dataGrid.getFooterRow(0)
        then:
        footer == addedFooter
    }

    protected void setDataGridItems(DataGrid dataGrid) {
        container = dataComponents.createCollectionContainer(GoodStatistic)
        container.setItems(createEntities())
        dataGrid.setItems(new ContainerDataGridItems(container))
    }

    protected List<GoodStatistic> createEntities() {
        GoodStatistic statistic1 = new GoodStatistic()
        statistic1.name = "stat1"
        statistic1.count = 20l
        statistic1.sales = 15
        statistic1.price = 120.5
        statistic1.usages = 25.5

        GoodStatistic statistic2 = new GoodStatistic()
        statistic2.name = "stat2"
        statistic2.count = 25l
        statistic2.sales = 20
        statistic2.price = 90.5
        statistic2.usages = 50.5
        return new ArrayList<GoodStatistic>(Arrays.asList(statistic1, statistic2))
    }
}
