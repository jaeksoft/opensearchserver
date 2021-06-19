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

import {CrawlFilterStatus} from "../types";
import {FormControl, InputLabel, MenuItem, Select} from "@material-ui/core";

interface Props {
  isDefault: boolean
  disabled: boolean,
  status: CrawlFilterStatus,
  setStatus: (newStatus: CrawlFilterStatus) => void,
}

const CrawlFilterStatusOption = ({disabled, status, setStatus, isDefault}: Props) => {

  const title = isDefault ? 'Default policy' : 'Policy';

  return (
    <FormControl fullWidth={true}>
      <InputLabel id="policy-label">{title}</InputLabel>
      <Select labelId="policy-label" id="policy" value={status}
              disabled={disabled}
              onChange={e => setStatus(e.target.value as CrawlFilterStatus)}>
        <MenuItem value={CrawlFilterStatus.accept}>Accept</MenuItem>
        <MenuItem value={CrawlFilterStatus.reject}>Reject</MenuItem>
      </Select>
    </FormControl>
  )
}

export default CrawlFilterStatusOption;
