/*
 * Copyright 2017-2021 Emmanuel Keller / Jaeksoft
 *  <p>
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import {CrawlFilterItem} from "../types";

interface CrawlFilterItemWithKey {
  key: number,
  filter: CrawlFilterItem
}

class CrawFilterListWithKey {

  private sequenceNumber: number;
  list: CrawlFilterItemWithKey[];

  constructor(crawlFilters: CrawlFilterItem[] | undefined) {
    this.sequenceNumber = 1;
    this.list = [];
    if (crawlFilters) {
      for (let crawlFilter of crawlFilters) {
        this.add(crawlFilter);
      }
    }
  }

  add(crawlFilterItem: CrawlFilterItem): CrawlFilterItem[] {
    delete (crawlFilterItem as any)['__typename']; //Remove Filter type, as the mutation expect a FilterInput
    const newItem: CrawlFilterItemWithKey = {key: this.sequenceNumber++, filter: crawlFilterItem};
    this.list.push(newItem);
    return this.getList();
  }

  save(itemToSave: CrawlFilterItem, keyToSave: number): CrawlFilterItem[] {
    const itemToUpdate = this.list.find(item => item.key === keyToSave);
    if (itemToUpdate) {
      itemToUpdate.filter = itemToSave;
    }
    return this.getList();
  }

  delete(key: number): CrawlFilterItem[] {
    this.list = this.list.filter(item => item.key !== key);
    return this.getList();
  }

  private getList(): CrawlFilterItem[] {
    let list: CrawlFilterItem[] = [];
    for (let crawlFilterItemWithKey of this.list) {
      list.push(crawlFilterItemWithKey.filter);
    }
    return list;
  }

}

export default CrawFilterListWithKey;
