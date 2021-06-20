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

export enum CrawlFilterStatus {
  accept = 'accept',
  reject = 'reject'
}

export interface CrawlFilterItem {
  pattern?: string;
  status: CrawlFilterStatus
}

export interface WebCrawlSettings {
  entryUrl: string
  maxDepth?: number
  filters?: CrawlFilterItem[]
  filterPolicy?: CrawlFilterStatus
}

export interface FileCrawlSettings {
  entryPath: string
  maxDepth?: number
  filters?: CrawlFilterItem[]
  filterPolicy?: CrawlFilterStatus
}

export interface CrawlStatus {
  startTime?: number
  endTime?: number
  crawled?: number
  rejected?: number
  error?: number
  running?: boolean
  aborting?: boolean
  abortingReason?: string
}

export interface CrawlNameStatus {
  name: string
  status: CrawlStatus
}

export interface CrawlRowProps {
  item: CrawlNameStatus
  completionCallback: () => void
}
