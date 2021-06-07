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

import {Box, Button, FormControl, InputLabel, MenuItem, Select, TextField} from "@material-ui/core";

import {CrawlFilterItem, CrawlFilterType} from "../types";
import {useEffect, useState} from "react";

interface Props {
  disabled: boolean,
  filter?: CrawlFilterItem,
  onAdd?: (item: CrawlFilterItem) => void,
  onSave?: (item: CrawlFilterItem) => void,
  onDelete?: (item: CrawlFilterItem) => void
}

const CrawlFilterItemView = (props: Props) => {

    const [pattern, setPattern] = useState<string>(props.filter?.pattern || '');
    const [type, setType] = useState<CrawlFilterType>((props.filter?.type) || CrawlFilterType.accept);
    const [hasChanges, setHashChanges] = useState<boolean>(false);

    const onAdd = () => {
      if (props.onAdd) {
        props.onAdd({pattern: pattern, type: type})
      }
    }

    const onSave = () => {
      if (props.filter && props.onSave) {
        props.onSave({pattern: pattern, type: type, index: props.filter.index});
      }
    }

    const onDelete = () => {
      if (props.filter && props.onDelete) {
        props.onDelete(props.filter);
      }
    }

    useEffect(() => {
      const b = pattern !== props.filter?.pattern || type !== props.filter?.type;
      setHashChanges(b);
    }, [props.filter, pattern, type])

    return (
      <Box display={"flex"} mb={2}>
        <Box flexGrow={1} mr={2}>
          <TextField id="filter" label="Wildcard filter" fullWidth={true}
                     value={pattern}
                     disabled={props.disabled}
                     onChange={e => setPattern(e.target.value)}
                     placeholder={"https://www.opensearchserver.com/*"}/>
        </Box>
        <Box mr={2}>
          <FormControl>
            <InputLabel id="policy-label">Policy</InputLabel>
            <Select labelId="policy-label" id="policy" value={type}
                    disabled={props.disabled}
                    onChange={e => setType(e.target.value as CrawlFilterType)}>
              <MenuItem value={CrawlFilterType.accept}>Accept</MenuItem>
              <MenuItem value={CrawlFilterType.reject}>Reject</MenuItem>
            </Select>
          </FormControl>
        </Box>
        <Box mr={2} display={"flex"} alignItems={"flex-end"}>
          {!props.filter &&
          <Button size={"small"} variant={"contained"} color={"primary"} disabled={!pattern.length || props.disabled}
                  onClick={onAdd}>Add
          </Button>
          }
          {props.filter && <>
            <Box mr={1}>
              <Button size={"small"} variant={"contained"} color={"primary"} disabled={!hasChanges || props.disabled}
                      onClick={onSave}>Save
              </Button>
            </Box>
            <Button size={"small"} variant={"contained"} color={"secondary"}
                    disabled={props.disabled}
                    onClick={onDelete}>Delete
            </Button>
          </>
          }
        </Box>
      </Box>
    )
  }
;

export default CrawlFilterItemView;
